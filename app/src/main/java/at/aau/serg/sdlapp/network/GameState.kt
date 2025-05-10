package at.aau.serg.sdlapp.network

import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.board.FieldType

/**
 * Repräsentiert den aktuellen Spielstatus mit Spielerpositionen
 */
data class GameState(
    // Map der Spielerpositionen: Spielername -> Position auf dem Spielbrett
    val playerPositions: Map<String, Int>,
    
    // Aktueller aktiver Spieler
    val currentPlayer: String,
    
    // Status des Spiels (z.B. "WAITING", "RUNNING", "FINISHED")
    val gameStatus: String
) {
    /**
     * Gibt das Spielfeld zurück, auf dem sich der Spieler befindet.
     * 
     * @param playerName Name des Spielers
     * @return Das Field-Objekt des Spielers oder null, wenn der Spieler nicht gefunden wurde
     */
    fun getPlayerField(playerName: String): Field? {
        val position = playerPositions[playerName] ?: return null
        
        // Einfache Feldgenerierung basierend auf der Spielerposition
        return Field(
            index = position,
            x = (position % 8) * 0.1f + 0.1f, // Einfache horizontale Positionierung
            y = (position / 8) * 0.1f + 0.1f, // Einfache vertikale Positionierung
            nextFields = listOf(position + 1), // Standardmäßig zum nächsten Feld
            type = FieldType.AKTION  // Standardwert aus der Field-Klasse
        )
    }
}