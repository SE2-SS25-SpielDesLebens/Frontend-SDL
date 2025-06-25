package at.aau.serg.sdlapp.model.player

import android.util.Log
import at.aau.serg.sdlapp.model.game.GameConstants
import at.aau.serg.sdlapp.model.player.PlayerRepository

/**
 * Verwaltet alle Spieler und ihre Positionen (Singleton)
 */
object PlayerManager {
    // Liste aller aktiven Spieler
    val players: Map<String, Player>
        get() = _players.toMap()
    private val _players = mutableMapOf<String, Player>()
    
    // Der lokale Spieler (dieser Client)
    private var localPlayerId: String = "1"
      /**
     * Fügt einen neuen Spieler hinzu
     */
    fun addPlayer(playerId: String, name: String, initialFieldIndex: Int = 0, color: CarColor = CarColor.BLUE): Player {
        val player = Player(playerId, name, initialFieldIndex)
        _players[playerId] = player
        return player
    }
      /**
     * Setzt den lokalen Spieler
     */
    fun setLocalPlayer(playerId: String, color: CarColor = CarColor.BLUE) {
        localPlayerId = playerId
        // Stelle sicher, dass der lokale Spieler in der Map existiert
        if (!players.containsKey(playerId)) {
            addPlayer(playerId, "Spieler $playerId", color = color)
        } else {
            // Wenn der Spieler bereits existiert, aktualisiere seine Farbe
            players[playerId]?.color = color
        }
    }
      /**
     * Gibt den lokalen Spieler zurück
     */
    fun getLocalPlayer(): Player? {
        val player = _players[localPlayerId]
        if (player == null) {
            Log.w("PlayerManager", "⚠️ getLocalPlayer(): Spieler $localPlayerId NICHT gefunden!")
        } else {
            Log.d("PlayerManager", "✅ getLocalPlayer(): $player")
        }
        return player
    }

    /**
     * Gibt eine Liste aller aktuell bekannten Spieler zurück
     */
    fun getAllPlayers(): Map<String, Player> = players

    /**
     * Gibt einen bestimmten Spieler anhand seiner ID zurück
     */
    fun getPlayer(playerId: String): Player? {
        return players[playerId]
    }
      /**
     * Aktualisiert die Position eines Spielers. Erstellt den Spieler, wenn er noch nicht existiert.
     */
    fun updatePlayerPosition(playerId: String, newFieldIndex: Int) {
        val player = players[playerId] ?: addPlayer(playerId, "Spieler $playerId")
        player.currentFieldIndex = newFieldIndex
    }

    /**
     * Prüft, ob es sich bei der ID um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: String): Boolean {
        return playerId == localPlayerId
    }
    
    /**
     * Synchronisiert die aktuelle Spielerliste mit der vom Server übermittelten Liste
     */
    fun syncWithActivePlayersList(activePlayerIds: List<String>): List<String> {
        val currentPlayers = players.keys.toSet()
        val removedPlayers = mutableListOf<String>()
        
        // Spieler entfernen, die nicht mehr in der Liste sind (außer lokaler Spieler)
        for (playerId in currentPlayers) {
            if (!activePlayerIds.contains(playerId) && playerId != localPlayerId) {
                _players.remove(playerId)
                Log.d("PlayerManager", "❌ Spieler $playerId entfernt")
                removedPlayers.add(playerId)
            }
        }
        return removedPlayers
    }

    /**
     * Entfernt einen Spieler aus der Map – aber nicht den lokalen Spieler!
     */
    fun removePlayer(playerId: String): Player? {
        // Den lokalen Spieler nicht entfernen
        if (playerId == localPlayerId) {
            return null
        }
        return _players.remove(playerId)
    }

    /**
     * Prüft, ob ein Spieler mit dieser ID existiert
     */
    fun playerExists(playerId: String): Boolean {
        return players.containsKey(playerId)
    }
    
    /**
     * Erstellt eine Debug-Zusammenfassung aller Spieler
     */
    fun getDebugSummary(): String {
        return "Spieler (${_players.size}): " +
                _players.values.joinToString(", ") {
                    "${it.id}:${it.color}" + if (it.id == localPlayerId) "*" else ""
                }
    }
    
    /**
     * Aktualisiert die Farbe eines Spielers
     */
    fun updatePlayerColor(playerId: String, colorName: String) {
        val player = players[playerId] ?: return
        
        // Konvertiere den String zur Enum
        val color = try {
            CarColor.valueOf(colorName)
        } catch (e: Exception) {
            println("❌ Fehler beim Konvertieren der Farbe: $colorName")
            return
        }
        
        // Setze die Farbe
        player.color = color
        println("🎨 Farbe für Spieler $playerId auf $colorName aktualisiert")
    }
    //Setzt das Startgeld für einen Spieler
    fun setStartMoneyForPlayer(playerId: String, viaUniversity: Boolean) {
        val player = _players[playerId] ?: return

        player.money = if (viaUniversity) 50_000 else 250_000
        Log.d("PlayerManager", "💸 Startgeld für $playerId gesetzt: ${player.money} (${if (viaUniversity) "Studium" else "Karriere"})")
    }


    fun getAllPlayersAsList() : List<Player> = players.values.toList()

    // Status, ob das Spiel bereits beendet wurde
    private var gameFinished = false

    fun markGameFinished() {
        gameFinished = true
    }

    fun isGameFinished(): Boolean = gameFinished

    /**
     * Prüft, ob ein oder alle Spieler auf dem Endfeld stehen
     */
    fun haveAllPlayersFinished(): Boolean {
        val allPlayers = getAllPlayersAsList()

        // Debug-Ausgabe
        allPlayers.forEach {
            Log.d("FinishCheck", "Spieler ${it.name} auf Feld ${it.currentFieldIndex}")
        }

        // Wenn nur 1 Spieler → genügt, wenn dieser auf einem Endfeld steht
        if (allPlayers.size == 1) {
            return allPlayers.first().currentFieldIndex in GameConstants.FINAL_FIELD_INDICES
        }

        // Sonst: alle müssen auf einem Endfeld sein
        return allPlayers.all { it.currentFieldIndex in GameConstants.FINAL_FIELD_INDICES }
    }



    suspend fun syncPlayerWithServer(playerId: String) {
        try {
            val updatedPlayer = PlayerRepository.fetchPlayerById(playerId)
            val player = _players[playerId] ?: addPlayer(playerId, updatedPlayer.id)

            player.money = updatedPlayer.money
            player.children = updatedPlayer.children
            player.hasEducation = updatedPlayer.education
            player.investments = updatedPlayer.investments
            player.salary = updatedPlayer.salary
            player.relationship = updatedPlayer.relationship

            Log.d("PlayerManager", "🔄 Spieler $playerId mit Serverdaten synchronisiert.")
        } catch (e: Exception) {
            Log.e("PlayerManager", "❌ Fehler beim Synchronisieren von $playerId: ${e.message}")
        }
    }





    fun clearPlayers(){
        _players.clear()
    }

    fun getAllPlayerIds() = _players.keys
}
