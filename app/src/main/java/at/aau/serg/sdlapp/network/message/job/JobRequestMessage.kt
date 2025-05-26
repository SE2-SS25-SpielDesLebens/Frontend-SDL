package at.aau.serg.sdlapp.network.message.job

data class JobRequestMessage(
    val playerName: String,
    val gameId: Int,
    val hasDegree: Boolean,
    val jobId: Int?
)
