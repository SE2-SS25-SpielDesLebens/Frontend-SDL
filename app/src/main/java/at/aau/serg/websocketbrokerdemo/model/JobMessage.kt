package at.aau.serg.websocketbrokerdemo.model

data class JobMessage(
    val playerName: String,
    val bezeichnung: String,
    val gehalt: Int,
    val bonusgehalt: Int,
    val benoetigtHochschulreife: Boolean,
    val isTaken: Boolean,
    val timestamp: String
)
