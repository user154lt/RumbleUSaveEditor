package ui.model

import data.Pokemon

data class ApplicationUIState(
    val isEditWindowShowing: Boolean = false,
    val pokemonForEditing: Pokemon = Pokemon()
)
