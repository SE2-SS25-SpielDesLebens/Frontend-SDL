package at.aau.serg.sdlapp.network.message.lobby

data class LobbyResponseMessage(
    val lobbyId : String,
    val playerName : String,
    val isSuccessful : Boolean,
    val message : String
)