package ui.model

import data.Pokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApplicationViewModel {

    private val _applicationUIState = MutableStateFlow( ApplicationUIState() )
    val applicationUIState: StateFlow<ApplicationUIState> = _applicationUIState.asStateFlow()

    fun openEditWindowWith(pokemon: Pokemon) =
        CoroutineScope(Dispatchers.Main).launch{
            _applicationUIState.update {
                it.copy(
                    isEditWindowShowing = true,
                    pokemonForEditing = pokemon,
                )
            }
        }

    fun closeEditWindow() =
        CoroutineScope(Dispatchers.Main).launch {
            _applicationUIState.update {
                it.copy(
                    isEditWindowShowing = false
                )
            }
        }
}