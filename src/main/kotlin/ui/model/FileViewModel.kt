package ui.model

import data.BlastSignatureException
import data.Pokemon
import data.PokemonRepository
import data.WorldSignatureException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ui.FileChooser
import java.io.FileNotFoundException

class FileViewModel(
    private val pokemonRepository: PokemonRepository,
    val selectForEditing: (Pokemon) -> Unit,
) {

    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    val pokemonList = pokemonRepository.loadedPokemon

    private val mainScope by lazy {
        CoroutineScope(Dispatchers.Main)
    }

    private val ioScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    fun chooseFolder() =
        mainScope.launch {
            val fileChooser = FileChooser()
            fileChooser.showFileChooser()?.let { folder ->
                _uiState.update {
                    it.copy(
                        folder = folder,
                        folderPath = formatPath(folder.path)
                    )
                }
            }
        }


    private fun formatPath(path: String) =
        if (path.length < 40) path else "${path.take(37)}..."

    fun decompressFiles() =
        ioScope.launch {
            uiState.value.folder?.let { folder ->
                try {
                    pokemonRepository.decompressFilesIn(folder)
                } catch (e: Exception) {
                    e.printStackTrace()
                    displayErrorMessage(e)
                }
            }
        }


    private fun displayErrorMessage(e: Exception) {
        val errorMessage = messageFor(e)
        _uiState.update {
            it.copy(
                message = errorMessage
            )
        }
    }

    private fun messageFor(e: Exception) =
        when (e) {
            is FileNotFoundException -> "Could not find files! \n Please check folder and try again"
            is BlastSignatureException -> "Rumble blast save detected! \n Editing not possible at this time"
            is WorldSignatureException -> "Rumble world save detected! \n Not compatible with this editor"
            else -> "An error occurred while decompressing. \n Please try again"
        }

    fun compressFile() =
        ioScope.launch {
            uiState.value.folder?.let {
                try {
                    pokemonRepository.compressFilesTo(it)
                    _uiState.update { currentState ->
                        currentState.copy(
                            message = "Files compressed successfully"
                        )
                    }
                } catch (e: Exception) {
                    displayCompressionError()
                }
            }
        }


    private fun displayCompressionError() =
        _uiState.update {
            it.copy(
                message = "Error compressing files! \n Please try again"
            )
        }

    fun dismissDialog() =
        mainScope.launch {
            _uiState.update {
                it.copy(
                    message = null,
                )
            }
        }

}