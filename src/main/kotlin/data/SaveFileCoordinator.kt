package data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Deflater.BEST_COMPRESSION
import java.util.zip.Inflater

const val BLAST_SIGNATURE = "cAVIAR1"
const val U_SIGNATURE = "cAVIAR2"
const val WORLD_SIGNATURE = "cAVAIAR4"
const val DEC_SLOT_START = 8
const val DEC_SLOT_SIZE = 67
const val SPECIES_OFFSET = 45
const val A_MOVE_OFFSET = 47
const val B_MOVE_OFFSET = 49
const val POWER_OFFSET = 51
const val POPULATED_BYTE = 0x3F

/**
 * Interacts with save file data. Decompresses save file data into memory ready for editing, recompresses when finished
 * then writes back to file.
 */
interface SaveFileCoordinator {
    companion object {
        fun getInstance() =
            DefaultSaveFileCoordinator() as SaveFileCoordinator
    }

    val loadedPokemon: StateFlow<List<Pokemon>>
    fun decompressFilesIn(saveFilesFolder: File)
    fun getPokemon(): List<Pokemon>
    fun updatePokemon(pokemon: Pokemon)
    fun compressFilesTo(saveFilesFolder: File)

}

fun ByteArray.toInt16() =
    ((this[0].toInt() and 0xFF) shl 8) or (this[1].toInt() and 0xFF)

fun Int.toBytes(number: Int = 4) =
    ByteArray(number) { (this shr 8 * (number - it - 1) and 0xFF).toByte() }


class DefaultSaveFileCoordinator : SaveFileCoordinator {

    enum class FileType {
        BLAST,
        U,
        WORLD,
        INVALID,
    }

    private val validFileNames = listOf(
        "00main.dat", "00pb00.dat", "00pb01.dat", "00pb02.dat",
        "00pb03.dat", "00pb04.dat"
    )

    private val _loadedPokemon = MutableStateFlow<List<Pokemon>>(listOf())
    override val loadedPokemon = _loadedPokemon.asStateFlow()

    private lateinit var mainBox: DataBox
    private lateinit var pokemonBoxes: List<DataBox>

    /*
        Get files in given folder that match save file names, validate signatures, then continue to decompress data.
     */
    override fun decompressFilesIn(saveFilesFolder: File) {
        saveFilesFolder.listFiles()?.let { filesList ->
            val saveFiles = filesList
                .filterNotNull()
                .filter { file -> validFileNames.any { name -> file.name == name } }
            if (saveFiles.size != validFileNames.size) throw FileNotFoundException("Could not find all save files")
            val signatureBytes = saveFiles.map { it.readBytes().slice(0..49) }
            try {
                validateFiles(signatureBytes)
            } catch (magicException: SignatureException) {
                throw magicException
            }
            populateBoxesWith(saveFiles)
            populatePokemonList()
        }
    }

    /*
        Ensure that save file signature is for the correct Pokémon rumble game, then check that zlib signature is
        present and in the correct place. Throw corresponding exception if not so that the information can be conveyed
        to the user.
     */
    private fun validateFiles(signatureBytes: List<List<Byte>>) {
        val saveSignatures = signatureBytes.map { it.slice(0..6) }
        val fileType = fileTypeFor(saveSignatures)
        if (fileType != FileType.U) throw signatureExceptionFor(fileType)
        val compressionSignatures = signatureBytes.map { it.slice(48..49) }
        if (!isValidZLibSignature(compressionSignatures)) throw NoZLibSignatureException()
    }

    private fun fileTypeFor(fileSignatures: List<List<Byte>>): FileType {
        val blastSignature = BLAST_SIGNATURE.toByteArray().toList()
        val uSignature = U_SIGNATURE.toByteArray().toList()
        val worldSignature = WORLD_SIGNATURE.toByteArray().toList()
        return when {
            fileSignatures.all { it == blastSignature } -> FileType.BLAST
            fileSignatures.all { it == uSignature } -> FileType.U
            fileSignatures.all { it == worldSignature } -> FileType.WORLD
            else -> FileType.INVALID
        }
    }

    private fun signatureExceptionFor(fileType: FileType) =
        when (fileType) {
            FileType.BLAST -> BlastSignatureException()
            FileType.WORLD -> WorldSignatureException()
            FileType.INVALID -> InvalidSignatureException()
            else -> InvalidSignatureException()
        }

    private fun isValidZLibSignature(compressionSignatures: List<List<Byte>>): Boolean {
        val zLibSignature = listOf(0x78.toByte(), 0xDA.toByte())
        return compressionSignatures.all { it == zLibSignature }

    }


    private fun populateBoxesWith(saveFiles: List<File>) {
        val mainBytes = saveFiles.first { it.name == "00main.dat" }.readBytes()
        mainBox = decompressedBoxFrom(mainBytes)
        val pokeBoxes = mutableListOf<DataBox>()
        for (i in 0..4) {
            val boxBytes = saveFiles.first { it.name == "00pb0$i.dat" }.readBytes()
            pokeBoxes.add(decompressedBoxFrom(boxBytes))
        }
        pokemonBoxes = pokeBoxes
    }

    /*
     * This is where decompression takes place. The original header is the first 48 (0x30) bytes, I store it in
     * memory for convenience, however it is quite simple to reconstruct it from scratch.
     */
    private fun decompressedBoxFrom(fileBytes: ByteArray) =
        DataBox(
            header = fileBytes.slice(0..47).toByteArray(),
            decompressedData = decompress(fileBytes.slice(48..<fileBytes.size).toByteArray())
        )

    private fun decompress(zLibStream: ByteArray): ByteArray {
        val inflater = Inflater()
        var result = ByteArray(20480)
        inflater.setInput(zLibStream)
        val size = inflater.inflate(result)
        inflater.end()
        result = result.slice(0..<size).toByteArray()
        return result
    }

    /*
     * This is where decompressed data is processed, the first 6 bytes of decompressed data are the signature
     * found in the validateDecompressed function. The first Pokémon slot starts at byte 8, each slot is 67 bytes
     * in size and starts with the bytes 0x02 0x3E.
     */
    private fun populatePokemonList() {
        val pokemonList = mutableListOf<Pokemon>()
        pokemonBoxes.forEach { box ->
            if (validateDecompressed(box.decompressedData.slice(0..5))) {
                for ((slot, i) in (DEC_SLOT_START..<box.decompressedData.size step DEC_SLOT_SIZE).withIndex()) {
                    if (i + 66 < box.decompressedData.size) {
                        pokemonFrom(box.decompressedData.slice(i..<(i + DEC_SLOT_SIZE)))?.let { pokemon ->
                            pokemonList.add(
                                pokemon.copy(
                                    box = pokemonBoxes.indexOf(box),
                                    slot = slot
                                )
                            )
                        }
                    }
                }
            }
        }
        _loadedPokemon.update {
            pokemonList
        }
    }

    private fun validateDecompressed(signature: List<Byte>): Boolean {
        val decompressedSignature = listOf(
            0x02.toByte(),
            0x85.toByte(),
            0x64.toByte(),
            0xC0.toByte(),
            0x04.toByte(),
            0x82.toByte(),
        )
        return signature == decompressedSignature
    }

    /*
        This is where each slot is processed. If a slot contains a Pokémon then the bytes at offsets 0x0E, 0x10, 0x12
        will all equal 0x3F. Species, moves and power modifier are all 16-bit integers. Species is at offset 0x2D,
        A move is at offset 0x2F, B move is at 0x31, and power modifier is at 0x33. The number held in the save file
        for the species is for the most part just the Pokédex value * 4. Some Pokémon that have different forms have an
        extra 2 added to this value, and some random Pokémon have an extra 1 added, however it seems that the game is
        happy to parse these in the same way that I have here, simply dividing by 4. Move numbers are simply stored as
        is. The value I refer to as the power modifier is not the actual power value, it is a value that has an
        exponential effect on the power. For example, the maximum value of 65535 will result in an 8 figure power value.
     */
    private fun pokemonFrom(slotBytes: List<Byte>): Pokemon? {
        var result: Pokemon? = null
        val signature = listOf(slotBytes[14], slotBytes[16], slotBytes[18])
        if (isSlotPopulated(signature)) {
            result = Pokemon(
                species = byteArrayOf(slotBytes[SPECIES_OFFSET], slotBytes[SPECIES_OFFSET + 1]).toInt16() / 4,
                aMove = Move(number = byteArrayOf(slotBytes[A_MOVE_OFFSET], slotBytes[A_MOVE_OFFSET + 1]).toInt16()),
                bMove = Move(number = byteArrayOf(slotBytes[B_MOVE_OFFSET], slotBytes[B_MOVE_OFFSET + 1]).toInt16()),
                powerModifier = byteArrayOf(slotBytes[POWER_OFFSET], slotBytes[POWER_OFFSET + 1]).toInt16(),
            )
        }
        return result
    }

    private fun isSlotPopulated(signature: List<Byte>) =
        signature.all { it == POPULATED_BYTE.toByte() }


    override fun getPokemon() = loadedPokemon.value

    /*
        This is where decompressed data is updated with new Pokémon values. It is essentially just the reverse of the
        pokemonFrom function.
     */
    override fun updatePokemon(pokemon: Pokemon) {
        updateListWith(pokemon)
        val slotStart = DEC_SLOT_START + pokemon.slot * DEC_SLOT_SIZE
        updateSpecies(slotStart, pokemon)
        updateMoves(slotStart, pokemon)
        updatePowerModifier(slotStart, pokemon)
    }

    private fun updateListWith(pokemon: Pokemon) {
        val newList = loadedPokemon.value.toMutableList().apply {
            val index = this.indexOf(this.first { it.box == pokemon.box && it.slot == pokemon.slot })
            this[index] = pokemon
        }
        _loadedPokemon.update {
            newList
        }
    }

    private fun updateSpecies(slotStart: Int, pokemon: Pokemon) {
        val speciesBytes = (pokemon.species * 4).toBytes(2)
        pokemonBoxes[pokemon.box].decompressedData[slotStart + SPECIES_OFFSET] = speciesBytes[0]
        pokemonBoxes[pokemon.box].decompressedData[slotStart + SPECIES_OFFSET + 1] = speciesBytes[1]
    }

    private fun updateMoves(slotStart: Int, pokemon: Pokemon) {
        val moveABytes = pokemon.aMove.number.toBytes(2)
        val moveBBytes = pokemon.bMove.number.toBytes(2)
        pokemonBoxes[pokemon.box].decompressedData[slotStart + A_MOVE_OFFSET] = moveABytes[0]
        pokemonBoxes[pokemon.box].decompressedData[slotStart + A_MOVE_OFFSET + 1] = moveABytes[1]
        pokemonBoxes[pokemon.box].decompressedData[slotStart + B_MOVE_OFFSET] = moveBBytes[0]
        pokemonBoxes[pokemon.box].decompressedData[slotStart + B_MOVE_OFFSET + 1] = moveBBytes[1]
    }

    private fun updatePowerModifier(slotStart: Int, pokemon: Pokemon) {
        val powerBytes = pokemon.powerModifier.toBytes(2)
        pokemonBoxes[pokemon.box].decompressedData[slotStart + POWER_OFFSET] = powerBytes[0]
        pokemonBoxes[pokemon.box].decompressedData[slotStart + POWER_OFFSET + 1] = powerBytes[1]
    }

    /*
        This is where box data from memory is recompressed and saved back to save files.
     */
    override fun compressFilesTo(saveFilesFolder: File) {
        val compressedMain = File(saveFilesFolder, "00main.dat")
        compressedMain.writeBytes(compressedBytesFrom(mainBox))
        for (i in 0..4) {
            val compressedBox = File(saveFilesFolder, "00pb0$i.dat")
            compressedBox.writeBytes(compressedBytesFrom(pokemonBoxes[i]))
        }
    }

    /*
        This is where the compression takes place. After compression the crc is calculated and the header updated
        with the new crc ready to be written back to the save files.
     */
    private fun compressedBytesFrom(box: DataBox): ByteArray {
        val compressed = compress(box.decompressedData)
        val crc = crcFor(compressed)
        box.header[8] = crc[0]
        box.header[9] = crc[1]
        box.header[10] = crc[2]
        box.header[11] = crc[3]
        return box.header.plus(compressed)
    }


    private fun compress(boxData: ByteArray): ByteArray {
        val deflater = Deflater(BEST_COMPRESSION)
        val result = ByteArray(4084)
        deflater.setInput(boxData)
        deflater.finish()
        val size = deflater.deflate(result)
        return result.slice(0..size).toByteArray()
    }

    private fun crcFor(compressedData: ByteArray): ByteArray {
        val crc32 = CRC32()
        crc32.update(compressedData)
        return crc32.value.toInt().toBytes(4)
    }


}