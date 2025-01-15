package ui.model

import data.Move
import data.MoveButton
import data.Pokemon
import ui.VisibleControl

data class EditPokemonUIState(
    val selected: Pokemon = Pokemon(
        spriteResource = "sprites/pkmn_002.png"
    ),
    val visibleControl: VisibleControl = VisibleControl.SPECIES,
    val moveToEdit: MoveButton = MoveButton.A_BTN,
    val pokemonList: List<Pokemon> = listOf(),
    val moves: List<Move> = listOf(),
    val filters: EditFilters = EditFilters(),
)
