package ui.model

import data.Move
import data.Pokemon

data class EditCallbacks(
    val editSpecies: (Pokemon) -> Unit = {},
    val editPowerFloat: (Float) -> Unit = {},
    val editPowerString: (String) -> Unit = {},
){
    var editMove: (Move) -> Unit = {}
}
