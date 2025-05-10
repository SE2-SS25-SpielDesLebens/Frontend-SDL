package at.aau.serg.sdlapp.network

/**
 * Repräsentiert den vom Backend gesendeten Spielstatus
 */
data class BackendGameState(
    // Map der Spielerpositionen: Spielername -> Position auf dem Spielbrett
    val playerPositions: Map<String, Int>,
    
    // ID des Spiels
    val gameId: String,
    
    // Liste der Felder im Spiel (wird aktuell nicht verwendet)
    val fields: List<Any>? = null
)