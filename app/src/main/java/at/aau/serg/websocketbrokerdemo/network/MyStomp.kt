package at.aau.serg.websocketbrokerdemo.network

import android.os.Handler
import android.os.Looper
import at.aau.serg.websocketbrokerdemo.Callbacks
import at.aau.serg.websocketbrokerdemo.model.JobMessage
import at.aau.serg.websocketbrokerdemo.model.OutputMessage
import at.aau.serg.websocketbrokerdemo.model.StompMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket" // Für Emulator! – anpassen bei echtem Gerät
//private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53217/websocket-broker/websocket"


class MyStomp(private val callbacks: Callbacks) {

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    fun connect() {
        val client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            try {
                session = client.connect(WEBSOCKET_URI)
                callback("✅ Verbunden mit Server")

                // Spielzug-Abo
                launch {
                    session.subscribeText("/topic/game").collect { msg ->
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        callback("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                    }
                }

                // Chat-Abo
                launch {
                    session.subscribeText("/topic/chat").collect { msg ->
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        callback("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                    }
                }

                // Job-Abo (einmalig)
                launch {
                    session.subscribeText("/topic/getJob").collect { msg ->
                        val job = gson.fromJson(msg, JobMessage::class.java)
                        val formatted = """
                            🎲 Spieler: ${job.playerName}
                            💼 Beruf: ${job.title}
                            💰 Gehalt: ${job.salary} €
                            🎁 Bonus: ${job.bonusSalary} €
                            🎓 Benötigt Matura: ${if (job.requiresHighSchoolDiploma) "Ja" else "Nein"}
                            🔒 Bereits vergeben: ${if (job.isTaken) "Ja, an ${job.takenByPlayerName ?: "unbekannt"}" else "Nein"}
                            🕒 Zeitpunkt: ${job.timestamp}
                        """.trimIndent()
                        callback(formatted)
                    }
                }

            } catch (e: Exception) {
                callback("❌ Fehler beim Verbinden: ${e.message}")
            }
        }
    }

    fun sendMove(player: String, action: String) {
        if (!::session.isInitialized) {
            callback("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, action = action)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                callback("✅ Spielzug gesendet")
            } catch (e: Exception) {
                callback("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }

    fun getJob(player: String, action: String) {
        if (!::session.isInitialized) {
            callback("❌ Fehler: Verbindung nicht aktiv!")
            return
        }

        val message = StompMessage(playerName = player, action = action)
        val json = gson.toJson(message)

        scope.launch {
            try {
                session.sendText("/app/getJob", json)
                callback("📤 Job-Anfrage gesendet")
            } catch (e: Exception) {
                callback("❌ Fehler beim Senden der Job-Anfrage: ${e.message}")
            }
        }
    }

    fun sendChat(player: String, text: String) {
        if (!::session.isInitialized) {
            callback("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, messageText = text)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/chat", json)
                callback("✅ Nachricht gesendet")
            } catch (e: Exception) {
                callback("❌ Fehler beim Senden (chat): ${e.message}")
            }
        }
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }
}
