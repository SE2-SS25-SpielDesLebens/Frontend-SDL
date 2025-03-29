package at.aau.serg.websocketbrokerdemo.model

data class OutputMessage(
    val playerName: String,
    val content: String,
    val timestamp: String
)
