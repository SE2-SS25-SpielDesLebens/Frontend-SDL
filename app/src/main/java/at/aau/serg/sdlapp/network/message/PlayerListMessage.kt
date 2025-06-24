package at.aau.serg.sdlapp.network.message
/**
 * Nachricht vom Server mit Liste aller aktiven Spieler
 */
data class PlayerListMessage(
    val type: String = "players",
    val playerList: List<String> = emptyList(),
    val timestamp: String = ""
)
