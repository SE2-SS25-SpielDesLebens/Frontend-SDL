package at.aau.serg.websocketbrokerdemo.model

data class StompMessage(
    val playerName: String,
    val action: String? = null,
    val messageText: String? = null
)
