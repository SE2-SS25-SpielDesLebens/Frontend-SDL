package at.aau.serg.sdlapp.network.message

data class StompMessage(
    val playerName: String,
    val action: String? = null,
    val messageText: String? = null,
    val gameId: String? = null
)

