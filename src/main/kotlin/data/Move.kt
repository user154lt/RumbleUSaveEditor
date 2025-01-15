package data

enum class MoveType(val typeName: String){
    NO_TYPE("No Type"),
    BUG("Bug"),
    GROUND("Ground"),
    DARK("Dark"),
    ICE("Ice"),
    DRAGON("Dragon"),
    NORMAL("Normal"),
    ELECTRIC("Electric"),
    POISON("Poison"),
    FIGHTING("Fighting"),
    PSYCHIC("Psychic"),
    FIRE("Fire"),
    ROCK("Rock"),
    FLYING("Flying"),
    STEEL("Steel"),
    GHOST("Ghost"),
    WATER("Water"),
    GRASS("Grass");

}

enum class MoveButton{
    A_BTN{
        override fun iconResource() =
            "btn_A.png"
    },
    B_BTN{
        override fun iconResource() =
            "btn_B.png"
    };

    abstract fun iconResource(): String
}

data class Move(
    val name: String = "",
    val number: Int = 0,
    val rating: Int = 0,
    val type: MoveType = MoveType.NORMAL,
)
