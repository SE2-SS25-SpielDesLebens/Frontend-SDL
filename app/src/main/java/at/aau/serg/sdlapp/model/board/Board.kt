package at.aau.serg.sdlapp.model.board


class Board(val fields: List<Field>) {

    private val playerPositions = mutableMapOf<String, Int>() // playerId → field index

    fun addPlayer(playerId: String, startFieldIndex: Int) {
        playerPositions[playerId] = startFieldIndex
    }

    fun movePlayer(playerId: String, steps: Int) {
        val currentFieldIndex = playerPositions[playerId] ?: 0
        val currentField = fields[currentFieldIndex]
        
        // Wenn das aktuelle Feld keine nextFields hat, kann nicht bewegt werden
        if (currentField.nextFields.isEmpty()) {
            return
        }
        
        // Bestimme das Zielfeld basierend auf dem Würfelwert
        val targetIndex = if (steps <= currentField.nextFields.size) {
            // Wenn der Würfelwert kleiner oder gleich der Anzahl der nextFields ist,
            // nehmen wir den Eintrag an der Position (steps - 1)
            // (weil Listen in Kotlin bei 0 beginnen)
            currentField.nextFields[steps - 1]
        } else {
            // Wenn der Würfelwert größer ist als die Anzahl der nextFields,
            // nehmen wir den letzten verfügbaren Eintrag
            currentField.nextFields.last()
        }
        
        // Aktualisiere die Position des Spielers
        playerPositions[playerId] = targetIndex
    }    fun getPlayerField(playerId: String): Field {
        val index = playerPositions[playerId] ?: 0
        return fields[index]
    }

    fun manualMoveTo(playerId: String, nextFieldIndex: Int) {
        // WICHTIG: Absicherung, dass der Move erlaubt ist
        val currentField = fields[playerPositions[playerId] ?: 0]
        if (nextFieldIndex in currentField.nextFields) {
            playerPositions[playerId] = nextFieldIndex
        }
    }
}
