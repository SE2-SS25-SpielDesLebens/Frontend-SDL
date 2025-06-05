package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.MoveMessage

/**
 * Verwaltet die Spielzüge und Bewegungen auf dem Spielbrett
 */
class BoardMoveManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val boardFigureManager: BoardFigureManager,
    private val callbacks: MoveCallbacks
) {
    // Speichert den aktuellen Field-Index
    private var currentFieldIndex = 0

    /**
     * Verarbeitet eine empfangene MoveMessage vom Server
     */
    fun handleMoveMessage(move: MoveMessage, localPlayerId: String, playerName: String, stompClient: StompConnectionManager) {
        val movePlayerId = move.playerId.toString()

        if (movePlayerId != "-1") {
            if (movePlayerId == localPlayerId) {
                println("🏠 LOKALER SPIELER bewegt sich")
                handleLocalPlayerMove(move)
            } else {
                println("🌍 REMOTE SPIELER (ID: $movePlayerId) bewegt sich")
                handleRemotePlayerMove(movePlayerId, move)
            }

            boardFigureManager.clearAllMarkers()
            addMarkersForNextPossibleFields(move, stompClient, playerName)
        } else {
            println("❌ Fehler: Spieler-ID ist -1, kann Bewegung nicht zuordnen")
            Toast.makeText(context, "Fehler: Ungültige Spieler-ID in Bewegungsnachricht", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verarbeitet die Bewegung des lokalen Spielers
     */
    private fun handleLocalPlayerMove(move: MoveMessage) {
        val playerId = move.playerId.toString()
        val oldFieldIndex = currentFieldIndex
        currentFieldIndex = move.fieldIndex
        println("🔄 Lokaler Feldindex aktualisiert: $oldFieldIndex -> ${move.fieldIndex}")

        playerManager.updatePlayerPosition(playerId, move.fieldIndex)

        val field = BoardData.board.find { it.index == move.fieldIndex }
        if (field != null) {
            println("🚗 BEWEGUNG STARTEN: Lokale Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y})")

            try {
                boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
                Toast.makeText(context, "Figur bewegt zu Feld ${move.fieldIndex}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                println("⚠️ FEHLER bei der Bewegung: ${e.message}")
                e.printStackTrace()

                try {
                    val playerFigure = boardFigureManager.getOrCreatePlayerFigure(playerId)
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
                            Toast.makeText(context, "Alternative Bewegung zu Feld ${move.fieldIndex}", Toast.LENGTH_SHORT).show()
                        } catch (e2: Exception) {
                            println("⚠️ Auch alternative Bewegung fehlgeschlagen: ${e2.message}")
                            e2.printStackTrace()
                        }
                    }, 500)
                } catch (e2: Exception) {
                    println("⚠️ Alternative Bewegung Vorbereitung fehlgeschlagen: ${e2.message}")
                    e2.printStackTrace()
                }
            }
        } else {
            println("❌ FEHLER: Feld mit Index ${move.fieldIndex} nicht gefunden!")
            Toast.makeText(context, "Feld ${move.fieldIndex} nicht gefunden!", Toast.LENGTH_LONG).show()

            val similarField = BoardData.board.minByOrNull { kotlin.math.abs(it.index - move.fieldIndex) }
            if (similarField != null) {
                println("🔍 Ähnlichstes Feld: ${similarField.index}")
                boardFigureManager.moveFigureToPosition(similarField.x, similarField.y, playerId)
            }
        }
    }

    /**
     * Verarbeitet die Bewegung eines entfernten Spielers
     */
    private fun handleRemotePlayerMove(playerId: String, moveMessage: MoveMessage) {
        if (!playerManager.playerExists(playerId)) {
            val player = playerManager.addPlayer(playerId, "Spieler $playerId")
            println("👤 Neuer Spieler erkannt: ID=$playerId, Farbe=${player.color}")
            boardFigureManager.playNewPlayerAnimation(playerId)
            Toast.makeText(context, "Neuer Spieler beigetreten: Spieler $playerId", Toast.LENGTH_SHORT).show()
        }

        playerManager.updatePlayerPosition(playerId, moveMessage.fieldIndex)

        val field = BoardData.board.find { it.index == moveMessage.fieldIndex }
        if (field != null) {
            boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
            println("🚗 Remote-Spieler $playerId zu Feld ${moveMessage.fieldIndex} bewegt")
        }

        callbacks.onPlayersChanged()
    }

    /**
     * Fügt Marker für die möglichen nächsten Felder hinzu
     */
    private fun addMarkersForNextPossibleFields(move: MoveMessage, stompClient: StompConnectionManager, playerName: String) {
        if (move.nextPossibleFields.isNotEmpty()) {
            println("🎯 Mögliche nächste Felder: ${move.nextPossibleFields.joinToString()}")

            val missingFields = move.nextPossibleFields.filter { nextIndex ->
                BoardData.board.none { it.index == nextIndex }
            }
            if (missingFields.isNotEmpty()) {
                println("⚠️ Warnung: Felder fehlen im Frontend: $missingFields")
            }

            move.nextPossibleFields.forEach { nextFieldIndex ->
                val nextField = BoardData.board.find { it.index == nextFieldIndex }
                if (nextField != null) {
                    boardFigureManager.addNextMoveMarker(nextField.x, nextField.y, nextFieldIndex, stompClient, playerName)
                }
            }
        }
    }

    /**
     * Platziert den Spieler auf dem angegebenen Startfeld
     */
    fun placePlayerAtStartField(playerId: String, fieldIndex: Int, stompClient: StompConnectionManager, playerName: String) {
        currentFieldIndex = fieldIndex
        playerManager.updatePlayerPosition(playerId, fieldIndex)

        val startField = BoardData.board.find { it.index == fieldIndex }
        if (startField != null) {
            boardFigureManager.moveFigureToPosition(startField.x, startField.y, playerId)
            println("🎮 Figur zum Startfeld bewegt: (${startField.x}, ${startField.y})")
        }

        stompClient.sendMove(playerName, "join:$fieldIndex")
        println("🎮 Sende join:$fieldIndex an Backend")
    }

    /**
     * Aktualisiert die Position eines Spielers, falls notwendig
     */
    fun updatePlayerPosition(playerId: String, fieldIndex: Int) {
        val current = playerManager.getPlayer(playerId)?.currentFieldIndex
        if (current != fieldIndex) {
            playerManager.updatePlayerPosition(playerId, fieldIndex)

            val field = BoardData.board.find { it.index == fieldIndex }
            if (field != null) {
                boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
            }
        }
    }

    /**
     * Gibt das aktuelle Feld des lokalen Spielers zurück
     */
    fun getCurrentFieldIndex(): Int {
        val local = playerManager.getLocalPlayer()
        Log.d("BoardMoveManager", "📍 getCurrentFieldIndex – localPlayer = $local")
        return local?.currentFieldIndex ?: currentFieldIndex
    }


    /**
     * Setzt den aktuellen Feldindex manuell
     */
    fun setCurrentFieldIndex(index: Int) {
        currentFieldIndex = index
    }

    /**
     * Interface für Move-Callbacks
     */
    interface MoveCallbacks {
        fun onPlayersChanged()
    }
}
