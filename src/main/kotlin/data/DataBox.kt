package data

data class DataBox(
    val header: ByteArray,
    val decompressedData: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        return when{
            (this === other) -> true
            (other is DataBox &&
                    (other.header.contentEquals(this.header) &&
                            other.decompressedData.contentEquals(this.decompressedData)))
                                -> true
            else -> false
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
