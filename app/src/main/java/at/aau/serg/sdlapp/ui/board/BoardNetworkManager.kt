package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.MoveMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.subscribeText
import java.util.Timer
import java.util.TimerTask
import at.aau.serg.sdlapp.network.viewModels.*

/**
 * Verwaltet die Netzwerkkommunikation der BoardActivity
 */
class BoardNetworkManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val playerName: String,
    private val playerId: String,
    private val callbacks: NetworkCallbacks,
    private val lobbyId: String? = null,
    private val stompClient: StompConnectionManager// Optional: Lobby-ID für Mehrspieler
) {
    // STOMP-Client für die Verbindung zum Backend

    
    // Timer für die periodische Aktualisierung der Spielerliste
    private var playerListUpdateTimer: Timer? = null
    
    init {
        initializeCallbacks()
        
        // Wenn eine Lobby-ID vorhanden ist, sofort verbinden
        if (!lobbyId.isNullOrEmpty()) {
            println("🎲 BoardNetworkManager: Verbinde mit Lobby $lobbyId")
            
            // Eine kleine Verzögerung, um sicherzustellen, dass UI initialisiert ist
            Handler(Looper.getMainLooper()).postDelayed({
                joinExistingGame(lobbyId)
            }, 500)
        }
    }    /**
     * Initialisiert alle Callbacks für den STOMP-Client
     */
    private fun initializeCallbacks() {
        // Handler für die Liste der aktiven Spieler
        stompClient.onPlayerListReceived = { playerIds ->
            Handler(Looper.getMainLooper()).post {
                println("👥 Liste der aktiven Spieler erhalten: $playerIds")

                // Übergebe Spielerliste an Callback-Methode
                callbacks.onPlayerListReceived(playerIds)
            }
        }
        
        // Handler für Farbänderungen von Spielern
        stompClient.onPlayerColorChanged = { playerId, colorName ->
            Handler(Looper.getMainLooper()).post {
                println("🎨 Farbänderung für Spieler $playerId: $colorName")
                
                // Aktualisiere die Spielerfarbe im PlayerManager
                playerManager.updatePlayerColor(playerId, colorName)
                
                // Übergebe Farbwechsel an Callback-Methode
                callbacks.onPlayerColorChanged(playerId, colorName)
            }
        }

        // Verbindungsstatus überwachen
        stompClient.onConnectionStateChanged = { isConnected ->
            Handler(Looper.getMainLooper()).post {
                callbacks.onConnectionStateChanged(isConnected)
                
                if (isConnected) {
                    // Nach einer erfolgreichen Verbindung fragen wir nach allen aktiven Spielern
                    requestActivePlayers()
                    println("👥 Frage nach aktiven Spielern...")

                    // Starte den Timer für periodische Aktualisierungen der Spielerliste
                    startPlayerListUpdateTimer()
                } else {
                    // Timer stoppen wenn die Verbindung verloren geht
                    stopPlayerListUpdateTimer()
                }
            }
        }

        // Behandlung von Verbindungsfehlern
        stompClient.onConnectionError = { errorMessage ->
            Handler(Looper.getMainLooper()).post {
                // In einer vollständigen Implementierung würde ein Dialog angezeigt werden
                println("🔴 Verbindungsfehler: $errorMessage")
                callbacks.onConnectionError(errorMessage)
            }
        }

        // Bewegung per Backend-Daten
        stompClient.onMoveReceived = { move ->
            Handler(Looper.getMainLooper()).post {
                try {
                    // Verbesserte Logging für Debugging mit mehr Details
                    println("📥 MoveMessage empfangen: Feld=${move.fieldIndex}, Typ=${move.typeString}, " +
                            "Spieler=${move.playerName} (ID=${move.playerId}), Nächste Felder=${move.nextPossibleFields.joinToString()}")

                    callbacks.onMoveReceived(move)
                } catch (e: Exception) {
                    println("❌❌❌ Unerwarteter Fehler bei der Bewegungsverarbeitung: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        // Handler für Board-Daten
        stompClient.onBoardDataReceived = { fields ->
            Handler(Looper.getMainLooper()).post {
                println("📊 Board-Daten vom Server erhalten (${fields.size} Felder)")
                callbacks.onBoardDataReceived(fields)
            }
        }
        
        // Handler für Spielerpositionen
        stompClient.onPlayerPositionsReceived = { positions ->
            Handler(Looper.getMainLooper()).post {
                println("📍 Spielerpositionen vom Server erhalten (${positions.size} Spieler)")
                callbacks.onPlayerPositionsReceived(positions)
            }
        }
    }

    /**
     * Sendet einen Würfelwurf an das Backend
     */
    fun sendRealMove(diceRoll: Int, currentFieldIndex: Int) {
        stompClient.sendRealMove(playerName, diceRoll, currentFieldIndex)
    }

    /**
     * Sendet eine einfache Bewegung zu einem Feld
     */
    fun sendMove(message: String) {
        stompClient.sendMove(playerName, message)
    }

    /**
     * Fragt aktive Spieler vom Server ab
     */
    fun requestActivePlayers() {
        stompClient.requestActivePlayers(playerName)
    }    /**
     * Fordert die aktuellen Board-Daten vom Server an
     */
    fun requestBoardData() {
        try {
            println("📊 Fordere Board-Daten vom Server an")
            stompClient.sendMessage("/app/board/data", "{\"request\":\"getBoard\"}")
            
            // Nach kurzer Verzögerung prüfen, ob wir eine Antwort bekommen haben
            Handler(Looper.getMainLooper()).postDelayed({
                if (at.aau.serg.sdlapp.model.board.BoardData.board.isNotEmpty()) {
                    println("✅ Board-Daten wurden geladen oder waren bereits verfügbar")
                } else {
                    println("⚠️ Keine Board-Daten nach Anfrage erhalten")
                    // Zeige eine Warnung an
                    Toast.makeText(
                        context,
                        "Warnung: Keine Board-Daten vom Server erhalten",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, 2000) // 2 Sekunden warten
            
        } catch (e: Exception) {
            println("❌ Fehler beim Anfordern der Board-Daten: ${e.message}")
            e.printStackTrace()
            
            // Fehler anzeigen
            Toast.makeText(
                context,
                "Fehler beim Anfordern der Brett-Daten",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Fordert die aktuellen Positionen aller Spieler vom Server an
     */
    fun requestPlayerPositions() {
        try {
            println("📍 Fordere Spielerpositionen vom Server an")
            stompClient.requestPlayerPositions()
        } catch (e: Exception) {
            println("❌ Fehler beim Anfordern der Spielerpositionen: ${e.message}")
            Toast.makeText(
                context,
                "Fehler beim Anfordern der Spielerpositionen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Prüft, ob die Verbindung besteht
     */
    val isConnected: Boolean
        get() = stompClient.isConnected
    
    /**
     * Gibt den STOMP-Client zurück
     */
    fun getStompClient(): StompConnectionManager {
        return stompClient
    }

    /**
     * Startet einen Timer für periodische Aktualisierungen der Spielerliste
     */
    private fun startPlayerListUpdateTimer() {
        // Vorherigen Timer stoppen falls vorhanden
        playerListUpdateTimer?.cancel()

        // Neuen Timer erstellen
        playerListUpdateTimer = Timer()
        playerListUpdateTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Anfrage nach Spielerliste senden
                requestActivePlayers()
                println("🔄 Automatische Anfrage nach Spielerliste gesendet")
                
                // Auch Positionen aller Spieler aktualisieren
                requestPlayerPositions()
                println("🔄 Automatische Anfrage nach Spielerpositionen gesendet")
            }
        }, 10000, 30000) // Initial nach 10 Sekunden, dann alle 30 Sekunden

        println("⏰ Spielerlisten-Update-Timer gestartet")
    }

    /**
     * Stoppt den Timer für die Spielerlisten-Aktualisierung
     */
    fun stopPlayerListUpdateTimer() {
        playerListUpdateTimer?.cancel()
        playerListUpdateTimer = null
        println("⏰ Spielerlisten-Update-Timer gestoppt")
    }    /**
     * Tritt einem bestehenden Spiel bei, das über die Lobby-ID identifiziert wird
     */    /**
     * Tritt einem existierenden Spiel in einer Lobby bei
     */
    private fun joinExistingGame(lobbyId: String) {
        try {
            println("🎮 Trete bestehendem Spiel in Lobby $lobbyId bei")
            
            // Stelle sicher, dass eine Verbindung besteht
            if (!stompClient.isConnected) {
                // Kurze Verzögerung für die Verbindung
                Handler(Looper.getMainLooper()).postDelayed({
                    completeGameJoin(lobbyId)
                }, 1000)
            } else {
                Log.e("BoardNetworkManager", "Keine Verbindung möglich")
                completeGameJoin(lobbyId)
            }
            
            Toast.makeText(
                context,
                "Trete Spiel in Lobby $lobbyId bei...",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            println("❌ Fehler beim Beitreten des Spiels: ${e.message}")
            e.printStackTrace()
            
            Toast.makeText(
                context,
                "Fehler beim Beitreten des Spiels: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
      /**
     * Führt den Spielbeitritt nach erfolgreicher Verbindung durch
     */
    private fun completeGameJoin(lobbyId: String) {
        // Starte den Spielbeitritt in einem Coroutine-Scope
        stompClient.run {
            scope.launch {
                try {
                    // 1. Lobby-Topic in separater Coroutine abonnieren, damit es nicht blockierend ist
                    scope.launch {
                        try {
                            sessionOrNull?.subscribeText("/topic/$lobbyId")?.collect { message ->
                                println("📩 Nachricht aus Lobby $lobbyId: $message")
                                // Je nach Inhalt verarbeiten (falls nötig)
                            }
                        } catch (e: Exception) {
                            println("❌ Fehler beim Abonnieren des Lobby-Topics: ${e.message}")
                        }
                    }
                    println("✅ Lobby-Topic abonniert: /topic/$lobbyId")
                    
                    // 2. Game-Status Topic in separater Coroutine abonnieren
                    scope.launch {
                        try {
                            sessionOrNull?.subscribeText("/topic/game/$lobbyId/status")?.collect { message ->
                                println("📢 Spielstatus: $message")
                            }
                        } catch (e: Exception) {
                            println("❌ Fehler beim Abonnieren des Status-Topics: ${e.message}")
                        }
                    }
                    println("✅ Spiel-Status-Topic abonniert: /topic/game/$lobbyId/status")
                    
                    // 3. Spielbeitritt zum Server senden
                    joinExistingGame(lobbyId, playerName)
                    
                    // 4. Nach kurzer Verzögerung Brettkonfiguration und Positionen anfordern
                    delay(1000)
                    requestBoardData()
                    requestPlayerPositions()
                    requestActivePlayers()
                } catch (e: Exception) {
                    println("❌ Fehler beim Spielbeitritt: ${e.message}")
                }
            }
        }
    }    /**
     * Interface für die Netzwerk-Callbacks
     */    interface NetworkCallbacks {
        fun onPlayerListReceived(playerIds: List<String>)
        fun onConnectionStateChanged(isConnected: Boolean)
        fun onConnectionError(errorMessage: String)
        fun onMoveReceived(move: MoveMessage)
        fun onPlayerColorChanged(playerId: String, colorName: String)

        /**
         * Wird aufgerufen, wenn Spiel-Brett-Daten vom Server empfangen wurden
         */
        fun onBoardDataReceived(fields: List<Field>)
        
        /**
         * Wird aufgerufen, wenn Spielerpositionen vom Server empfangen wurden
         */
        fun onPlayerPositionsReceived(positions: Map<String, Int>)
    }
}
