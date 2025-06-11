package at.aau.serg.sdlapp.network.message.house

data class HouseMessage(
    val houseId: Int,
    val bezeichnung: String,
    val kaufpreis: Int,
    val verkaufspreisRot: Int,
    val verkaufspreisSchwarz: Int,
    val isTaken: Boolean,
    val assignedToPlayerName: String?,
    val gameId: Int,
    val sellPrice: Boolean
)
