package ui.model

import data.MoveType

data class EditFilterCallbacks(
    val updatePokemonNameFilter: (String) -> Unit = {},
    val updateMoveNameFilter: (String) -> Unit = {},
    val toggleRatingVisible: () -> Unit = {},
    val toggleRatingFilter: (Int) -> Unit = {},
    val toggleTypeVisible: () -> Unit = {},
    val toggleTypeFilter: (MoveType) -> Unit = {},
)
