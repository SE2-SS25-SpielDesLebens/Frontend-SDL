package at.aau.serg.sdlapp.network

/**
 * Antwort auf eine Spielaktion vom Server
 */
data class GameActionResponse(
    // Der aktualisierte Spielstatus
    val gameState: BackendGameState? = null,
    
    // Das Ergebnis einer Bewegung
    val moveResult: MoveResult? = null,
    
    // Nachricht vom Server
    val message: String? = null,
    
    // Ob die Aktion erfolgreich war
    val success: Boolean = true
)

/**
 * Ergebnis einer Spielerbewegung
 */
data class MoveResult(
    // Die aktuelle Position des Spielers
    val position: Int,
    
    // Ob eine Auswahl erforderlich ist
    val requiresChoice: Boolean = false,
    
    // Die verfügbaren Optionen, wenn eine Auswahl erforderlich ist
    val options: List<Int>? = null
)