package at.aau.serg.sdlapp.network

data class OutputMessage(
    val playerName: String,
    val position: Int? = null,
    val content: String? = null, // Von Backend als 'content' gesendet
    val options: List<Int>? = null,
    val timestamp: String? = null
)
