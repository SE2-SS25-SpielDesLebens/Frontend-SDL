package at.aau.serg.sdlapp.network.message.house

data class HouseBuyElseSellMessage(
    var playerID: String?,
    var gameId: Int,
    var buyElseSell: Boolean
)