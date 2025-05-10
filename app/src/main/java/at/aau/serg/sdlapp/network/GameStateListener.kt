package at.aau.serg.sdlapp.network

/**
 * Interface für Benachrichtigungen über Änderungen im Spielstatus
 */
interface GameStateListener {
    /**
     * Wird aufgerufen, wenn der Spielstatus aktualisiert wurde
     */
    fun onGameStateUpdated(gameState: GameState)
    
    /**
     * Wird aufgerufen, wenn der Spieler eine Auswahl zwischen verschiedenen Feldern treffen muss
     */
    fun onChoiceRequired(options: List<Int>)
    
    /**
     * Wird aufgerufen, wenn ein Fehler auftritt
     */
    fun onError(message: String)
}