package at.aau.serg.sdlapp.model

data class StompMessage(
    val playerName: String,
    val action: String? = null,
    val messageText: String? = null,
    val gameId: String = "1" // default für Tests
)

