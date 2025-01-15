package ui.model

import data.MoveType

data class EditFilters(
    val pokemonName: String = "",
    val moveName: String = "",
    val isRatingVisible: Boolean = false,
    val ratings: List<Boolean> = listOf(
        false,
        false,
        false,
        false,
        false,
        false,
    ),
    val isTypesVisible: Boolean = false,
    val types: Map<MoveType, Boolean> = mapOf(
        MoveType.NO_TYPE to false,
        MoveType.BUG to false,
        MoveType.GROUND to false,
        MoveType.DARK to false,
        MoveType.ICE to false,
        MoveType.DRAGON to false,
        MoveType.NORMAL to false,
        MoveType.ELECTRIC to false,
        MoveType.POISON to false,
        MoveType.FIGHTING to false,
        MoveType.PSYCHIC to false,
        MoveType.FIRE to false,
        MoveType.ROCK to false,
        MoveType.FLYING to false,
        MoveType.STEEL to false,
        MoveType.GHOST to false,
        MoveType.WATER to false,
        MoveType.GRASS to false,
    )
)
