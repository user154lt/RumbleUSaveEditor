package data

data class Pokemon(
    val speciesName: String = "",
    val spriteResource: String = "sprites/pkmn_001.png",
    val species: Int = 0,
    val aMove: Move = Move(),
    val bMove: Move = Move(),
    val powerModifier: Int = 0,
    val box: Int = 0,
    val slot: Int = 0,
)

