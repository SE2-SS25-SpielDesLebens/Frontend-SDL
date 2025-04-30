package at.aau.serg.websocketbrokerdemo.model

data class ActionCard (
    val headline : String,
    val action : String,
    val imageName : String,
    val choices : Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionCard

        if (headline != other.headline) return false
        if (action != other.action) return false
        if (imageName != other.imageName) return false
        if (!choices.contentEquals(other.choices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headline.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + imageName.hashCode()
        result = 31 * result + choices.contentHashCode()
        return result
    }
}