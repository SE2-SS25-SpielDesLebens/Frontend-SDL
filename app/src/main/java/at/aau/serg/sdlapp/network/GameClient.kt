package at.aau.serg.sdlapp.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.stomp.headers.StompConnectHeaders
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import at.aau.serg.sdlapp.model.board.Field
import okhttp3.OkHttpClient
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Client für die Kommunikation mit dem Spielserver über WebSockets/STOMP
 * Verbindet das Board im Frontend mit dem Backend
 */
class GameClient(
    private val playerName: String,
    private val gameId: String,
    private val listener: GameStateListener
) {    companion object {
        private const val TAG = "GameClient"
        // Mehrere Verbindungs-URLs, die wir nacheinander versuchen können
        private val WEBSOCKET_URIS = listOf(
            "ws://10.0.2.2:8080/websocket-broker",  // Emulator -> Host PC
            "ws://localhost:8080/websocket-broker", // Lokaler Test
            "ws://127.0.0.1:8080/websocket-broker"  // Alternative lokale IP
        )
        private const val CONNECTION_TIMEOUT_MS = 10000L // 10 Sekunden Timeout
    }

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    /**
     * Verbindet zum Server und abonniert relevante Topics
     */    fun connect() {
        scope.launch {
            var lastException: Exception? = null
            
            // Versuche alle verfügbaren URLs
            for (uri in WEBSOCKET_URIS) {
                try {
                    Log.d(TAG, "Verbinde mit Server: $uri")
                    
                    // Konfiguriere OkHttp mit höheren Timeouts
                    val okHttpClient = OkHttpClient.Builder()
                        .connectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .readTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .writeTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .build()
                    
                    val webSocketClient = OkHttpWebSocketClient(okHttpClient)                    // Konfiguriere STOMP Client mit Standardeinstellungen
                    val stompConfig = StompConfig()
                    val client = StompClient(webSocketClient, stompConfig)
                    
                    // Versuche Verbindung mit Timeout
                    try {                        withTimeout(CONNECTION_TIMEOUT_MS) {
                            Log.d(TAG, "Versuche Verbindung zu $uri...")
                            
                            // In Version 4.5.0 einfach ohne zusätzliche Header verbinden
                            session = client.connect(uri)
                            
                            Log.d(TAG, "✅ Verbindung hergestellt zu $uri!")
                            
                            // Erfolgreich verbunden - informiere sofort den Listener
                            val dummyGameState = GameState(
                                playerPositions = mapOf(playerName to 0),
                                currentPlayer = playerName,
                                gameStatus = "CONNECTED"
                            )
                            listener.onGameStateUpdated(dummyGameState)
                            
                            // Topic für Spielzustandsupdates abonnieren
                            session.subscribeText("/topic/game").collect { message ->
                                handleGameMessage(message)
                            }
                            
                            // Lobby-Topic für die Spielerstellung und -beitritt abonnieren
                            session.subscribeText("/topic/lobby").collect { message ->
                                handleLobbyMessage(message)
                            }
                            
                            // Nach erfolgreicher Verbindung eine Lobby erstellen
                            createGame()
                        }
                        
                        // Wenn wir hier ankommen, war die Verbindung erfolgreich
                        return@launch
                    } catch (e: TimeoutCancellationException) {
                        Log.w(TAG, "Timeout beim Verbindungsversuch zu $uri")
                        lastException = e
                    } catch (e: Exception) {
                        val message = "Verbindungsfehler zu $uri: ${e.message}. Grund: ${e.cause?.message ?: "Unbekannt"}"
                        Log.e(TAG, message, e)
                        lastException = e
                    }
                    
                    // Kurz warten, bevor wir die nächste URL versuchen
                    kotlinx.coroutines.delay(500)
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler bei Verbindungsversuch zu $uri", e)
                    lastException = e
                }
            }
              // Wenn wir hier ankommen, waren alle Verbindungsversuche erfolglos
            // Führe eine Netzwerkdiagnose durch, um das Problem zu analysieren
            val diagnosticResults = withContext(Dispatchers.IO) {
                val results = StringBuilder()
                results.appendLine("Verbindung fehlgeschlagen zu allen Server-URLs.")
                
                // Teste, ob der Server überhaupt erreichbar ist
                val connectivityTests = NetworkUtils.testConnections(8080)
                results.appendLine("Konnektivitätsdiagnose:")
                
                connectivityTests.forEach { (host, reachable) ->
                    results.appendLine("- $host: ${if (reachable) "erreichbar ✓" else "nicht erreichbar ✗"}")
                }
                
                results.toString()
            }
            
            val errorMsg = "Verbindung fehlgeschlagen: $diagnosticResults\nLetzter Fehler: ${lastException?.message}"
            Log.e(TAG, errorMsg, lastException)
            listener.onError(errorMsg)
        }
    }

    /**
     * Erstellt ein neues Spiel
     */
    private fun createGame() {
        sendLobbyMessage("createLobby")
    }

    /**
     * Lässt den Spieler dem Spiel beitreten mit Auswahl des Startfeldes
     * @param startFieldIndex Index des Startfeldes (0 für normalen Start, 5 für Uni-Start)
     */
    fun joinGame(startFieldIndex: Int) {
        val request = createGameActionRequest()
        request.addProperty("startFieldIndex", startFieldIndex)
        
        scope.launch {
            try {
                session.sendText("/app/game/joinGame", gson.toJson(request))
                Log.d(TAG, "Spiel beigetreten mit Startfeld $startFieldIndex")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Spielbeitritt", e)
                listener.onError("Fehler beim Spielbeitritt: ${e.message}")
            }
        }
    }

    /**
     * Bewegt den Spieler um die angegebene Anzahl Felder
     * @param steps Anzahl der Schritte
     */
    fun movePlayer(steps: Int) {
        val request = createGameActionRequest()
        request.addProperty("steps", steps)
        
        scope.launch {
            try {
                session.sendText("/app/game/movePlayer", gson.toJson(request))
                Log.d(TAG, "Spieler bewegt: $steps Schritte")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler bei der Spielerbewegung", e)
                listener.onError("Fehler bei der Spielerbewegung: ${e.message}")
            }
        }
    }

    /**
     * Wählt ein bestimmtes Feld für die Bewegung (z.B. bei Verzweigungen)
     * @param fieldIndex Index des gewählten Feldes
     */
    fun chooseField(fieldIndex: Int) {
        val request = createGameActionRequest()
        request.addProperty("fieldIndex", fieldIndex)
        
        scope.launch {
            try {
                session.sendText("/app/game/chooseField", gson.toJson(request))
                Log.d(TAG, "Feld gewählt: $fieldIndex")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler bei der Feldwahl", e)
                listener.onError("Fehler bei der Feldwahl: ${e.message}")
            }
        }
    }

    /**
     * Sendet eine Chatnachricht
     * @param message Die zu sendende Nachricht
     */
    fun sendChatMessage(message: String) {
        val stompMessage = StompMessage(playerName, null, message, gameId)
        
        scope.launch {
            try {
                session.sendText("/app/chat", gson.toJson(stompMessage))
                Log.d(TAG, "Chat-Nachricht gesendet: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden der Nachricht", e)
                listener.onError("Fehler beim Senden der Nachricht: ${e.message}")
            }
        }
    }

    /**
     * Sendet eine Nachricht an das Lobby-Topic
     * @param action Die auszuführende Aktion ("createLobby", "joinLobby", etc.)
     */
    private fun sendLobbyMessage(action: String) {
        val stompMessage = StompMessage(playerName, action, null, gameId)
        
        scope.launch {
            try {
                session.sendText("/app/lobby", gson.toJson(stompMessage))
                Log.d(TAG, "Lobby-Aktion gesendet: $action")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden der Lobby-Aktion", e)
                listener.onError("Fehler beim Senden der Lobby-Aktion: ${e.message}")
            }
        }
    }

    /**
     * Verarbeitet eine Nachricht vom Game-Topic
     */
    private fun handleGameMessage(message: String) {
        try {
            Log.d(TAG, "Game-Nachricht empfangen: $message")
            
            // Prüfen, ob es eine GameActionResponse ist
            if (message.contains("gameState")) {
                val response = gson.fromJson(message, GameActionResponse::class.java)
                
                if (response.gameState != null) {
                    listener.onGameStateUpdated(mapToGameState(response.gameState))
                }
                
                if (response.moveResult != null && response.moveResult.requiresChoice) {
                    listener.onChoiceRequired(response.moveResult.options ?: emptyList())
                }
            } else {
                // Normale OutputMessage
                val output = gson.fromJson(message, OutputMessage::class.java)
                Log.d(TAG, "Spielzug: ${output.playerName}: ${output.content}")
                
                // Options können auch hier enthalten sein
                if (output.options != null && output.options.isNotEmpty()) {
                    listener.onChoiceRequired(output.options)
                }
                
                // Wenn Position vorhanden ist, dann als Zustandsupdate behandeln
                if (output.position != null) {
                    val dummyGameState = GameState(
                        mapOf(output.playerName to output.position),
                        output.playerName,
                        "RUNNING"
                    )
                    listener.onGameStateUpdated(dummyGameState)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Verarbeiten der Game-Nachricht", e)
            listener.onError("Fehler beim Verarbeiten der Spielnachricht: ${e.message}")
        }
    }

    /**
     * Verarbeitet eine Nachricht vom Lobby-Topic
     */
    private fun handleLobbyMessage(message: String) {
        try {
            Log.d(TAG, "Lobby-Nachricht empfangen: $message")
            val output = gson.fromJson(message, OutputMessage::class.java)
            
            // Wenn Lobby erstellt oder beigetreten, können wir hier reagieren
            if (output.content?.contains("erstellt") == true || 
                output.content?.contains("beigetreten") == true) {
                // Spieler hat erfolgreich eine Lobby erstellt oder ist beigetreten
                Log.d(TAG, "Lobby-Aktion erfolgreich: ${output.content}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Verarbeiten der Lobby-Nachricht", e)
        }
    }

    /**
     * Erstellt ein Basis-RequestObject für Game-Aktionen
     */
    private fun createGameActionRequest(): JsonObject {
        val request = JsonObject()
        request.addProperty("gameId", gameId)
        request.addProperty("playerName", playerName)
        return request
    }

    /**
     * Konvertiert einen Backend-GameState in einen Frontend-GameState
     */
    private fun mapToGameState(backendState: BackendGameState): GameState {
        return GameState(
            playerPositions = backendState.playerPositions,
            currentPlayer = playerName, // Nehmen wir den aktuellen Spieler an
            gameStatus = "RUNNING" // Wir gehen von einem laufenden Spiel aus
        )
    }    /**
     * Schließt die Verbindung
     */
    fun disconnect() {
        scope.launch {
            try {
                if (::session.isInitialized) {
                    // Verbindung trennen
                    session.disconnect()
                    Log.d(TAG, "Verbindung getrennt")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Trennen der Verbindung", e)
            }
        }
    }
}