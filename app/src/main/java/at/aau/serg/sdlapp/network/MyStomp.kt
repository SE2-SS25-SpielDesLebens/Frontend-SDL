package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

// Die URL in der Dokumentation ist "/websocket-example-broker",
// aber der Code im Backend sagt "/websocket-broker"
// Für Emulatoren: 10.0.2.2 verweist auf localhost des Host-Computers
private val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker"
private const val TAG = "MyStomp"

class MyStomp(private val callback: (String) -> Unit) {
    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    fun connect() {
        val client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            try {
                Log.d(TAG, "Verbindungsversuch zu $WEBSOCKET_URI")
                session = client.connect(WEBSOCKET_URI)
                Log.i(TAG, "Verbindung erfolgreich hergestellt")
                sendToMainThread("✅ Verbunden mit Server")

                // Topic für Spielaktionen
                Log.d(TAG, "Abonniere /topic/game")
                session.subscribeText("/topic/game").collect { msg ->
                    Log.d(TAG, "Nachricht vom Game-Topic empfangen: $msg")
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fehler beim Deserialisieren der Game-Nachricht", e)
                        sendToMainThread("❌ Fehler beim Verarbeiten der Spielnachricht: ${e.message}")
                    }
                }

                // Topic für Chat
                Log.d(TAG, "Abonniere /topic/chat")
                session.subscribeText("/topic/chat").collect { msg ->
                    Log.d(TAG, "Nachricht vom Chat-Topic empfangen: $msg")
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fehler beim Deserialisieren der Chat-Nachricht", e)
                        sendToMainThread("❌ Fehler beim Verarbeiten der Chatnachricht: ${e.message}")
                    }
                }

                // Topic für Lobby-Aktionen
                Log.d(TAG, "Abonniere /topic/lobby")
                session.subscribeText("/topic/lobby").collect { msg ->
                    Log.d(TAG, "Nachricht vom Lobby-Topic empfangen: $msg")
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("🏠 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fehler beim Deserialisieren der Lobby-Nachricht", e)
                        sendToMainThread("❌ Fehler beim Verarbeiten der Lobbynachricht: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Verbinden", e)
                sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            }
        }
    }

    fun sendMove(player: String, action: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        
        Log.d(TAG, "Sende Move: $player, $action")
        val message = StompMessage(playerName = player, action = action)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                sendToMainThread("✅ Spielzug gesendet")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden des Moves", e)
                sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }

    fun sendRealMove(player: String, dice: Int) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        
        Log.d(TAG, "Sende Würfelwurf: $player, $dice")
        val message = StompMessage(playerName = player, action = "$dice gewürfelt")
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                sendToMainThread("✅ Spielzug gesendet")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden des Würfelwurfs", e)
                sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }

    fun sendChat(player: String, text: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        
        Log.d(TAG, "Sende Chat: $player, $text")
        val message = StompMessage(playerName = player, messageText = text)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/chat", json)
                sendToMainThread("✅ Nachricht gesendet")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden der Chatnachricht", e)
                sendToMainThread("❌ Fehler beim Senden (chat): ${e.message}")
            }
        }
    }
    
    fun sendLobbyAction(player: String, action: String, gameId: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        
        Log.d(TAG, "Sende Lobby-Aktion: $player, $action, $gameId")
        val message = StompMessage(playerName = player, action = action, gameId = gameId)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/lobby", json)
                sendToMainThread("✅ Lobby-Aktion gesendet")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Senden der Lobby-Aktion", e)
                sendToMainThread("❌ Fehler beim Senden (lobby): ${e.message}")
            }
        }
    }

    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
