package data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

interface PokemonRepository {
    companion object {
        fun getInstance() =
            DefaultPokemonRepository() as PokemonRepository
    }

    val loadedPokemon: StateFlow<List<Pokemon>>
    fun decompressFilesIn(saveFilesFolder: File)
    fun getSavedPokemon(): List<Pokemon>
    fun updatePokemon(pokemon: Pokemon)
    fun compressFilesTo(saveFilesFolder: File)
    fun fullPokemonList(): List<Pokemon>
    fun dataFor(species: Int): Pokemon
    fun fullMoveList(): List<Move>

}

class DefaultPokemonRepository : PokemonRepository {

    private val saveFileCoordinator = SaveFileCoordinator.getInstance()

    private val pokemonDataProvider by lazy {
        PokemonDataProvider.getInstance()
    }


    override val loadedPokemon = saveFileCoordinator.loadedPokemon
        .map { pokemonList ->
            pokemonList.map {
                val data = pokemonDataProvider.dataFor(it.species)
                it.copy(
                    speciesName = data.speciesName,
                    spriteResource = data.spriteResource,
                    aMove = pokemonDataProvider.moveDataFor(it.aMove.number),
                    bMove = pokemonDataProvider.moveDataFor(it.bMove.number)
                )
            }
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )


    override fun decompressFilesIn(saveFilesFolder: File) =
        saveFileCoordinator.decompressFilesIn(saveFilesFolder)


    override fun getSavedPokemon() =
        saveFileCoordinator.getPokemon().map {
            val data = pokemonDataProvider.dataFor(it.species)
            it.copy(
                speciesName = data.speciesName,
                spriteResource = data.spriteResource,
                aMove = pokemonDataProvider.moveDataFor(it.aMove.number),
                bMove = pokemonDataProvider.moveDataFor(it.bMove.number)
            )
        }

    override fun updatePokemon(pokemon: Pokemon){
        saveFileCoordinator.updatePokemon(pokemon)
    }

    override fun compressFilesTo(saveFilesFolder: File) =
        saveFileCoordinator.compressFilesTo(saveFilesFolder)

    override fun fullPokemonList() =
        pokemonDataProvider.fullList()

    override fun dataFor(species: Int) =
        pokemonDataProvider.dataFor(species)

    override fun fullMoveList() =
        pokemonDataProvider.fullMoveList()
}