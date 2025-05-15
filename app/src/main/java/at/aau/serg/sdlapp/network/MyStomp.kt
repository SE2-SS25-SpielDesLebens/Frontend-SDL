package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONException
import org.json.JSONObject

//private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53217/websocket-broker/websocket"
private const val WEBSOCKET_URI = "ws://192.168.8.133:8080/websocket-broker/websocket"

class MyStomp(private val callback: (String) -> Unit) {

    private var session: StompSession? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    private val _lobbyUpdates = MutableSharedFlow<LobbyResponseMessage>()
    private var isConnected: Boolean = false
    val lobbyUpdates: SharedFlow<LobbyResponseMessage> = _lobbyUpdates.asSharedFlow()

    fun getSession(): StompSession? = synchronized(this) {
        return@synchronized if (isConnected) session else null
    }

    suspend fun connect(playerName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = StompClient(OkHttpWebSocketClient())
            session = client.connect(url = WEBSOCKET_URI, login = playerName)
            isConnected = true

            sendToMainThread("✅ Verbunden mit Server")

            launchMessageCollectors()
            true
        } catch (e: Exception) {
            sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            isConnected = false
            false
        }
    }

    fun connectAsync(playerName: String, onResult: (Boolean) -> Unit = {}) {
        scope.launch {
            val result = connect(playerName)
            withContext(Dispatchers.IO) {
                onResult(result)
            }
        }
    }

    private fun CoroutineScope.launchMessageCollectors() {
        session?.let { s ->
            launch {
                s.subscribeText("/topic/game").collect { msg ->
                    val output = gson.fromJson(msg, OutputMessage::class.java)
                    sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                }
            }

            launch {
                s.subscribeText("/topic/chat").collect { msg ->
                    val output = gson.fromJson(msg, OutputMessage::class.java)
                    sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                }
            }
        }
    }


    /**
     * Sendet den Spielstart und abonniert im gleichen Schritt das Job-Topic.
     */
    fun sendGameStart(gameId: Int, playerName: String) {
        getSession()?.let {
            scope.launch {
                try {
                    session?.sendText("/app/game/start/$gameId", "")
                    sendToMainThread("📨 Spielstart gesendet, Player=$playerName")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden des Spielstarts: ${e.message}")
                }
            }
        } ?: sendToMainThread("Keine Verbindung aktiv")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun sendLobbyCreate(playerName: String): String? = withContext(Dispatchers.IO) {
        val session = getSession() ?: run {
            sendToMainThread("Not connected")
            return@withContext null
        }
        try {

            val request = LobbyRequestMessage(playerName)
            val json = gson.toJson(request)

            val flow = session.subscribeText("/user/queue/lobby/created")


            session.sendText("/app/lobby/create", json)
            sendToMainThread("Lobby wird erstellt")

            Log.d("Debugging", "got here")

            val response = flow.first()

            val lobbyId = JSONObject(response).getString("lobbyID").also {
                Log.d("LobbyFlow", "Created lobby: $it")
            }

            val updateFlow = session.subscribeText("/topic/$lobbyId")
            Log.d("Debugging", "finished subscription")

            scope.launch {
                updateFlow.collect { payload ->
                    try {
                        _lobbyUpdates.emit(
                            gson.fromJson(
                                payload,
                                LobbyResponseMessage::class.java
                            )
                        )
                    } catch (e: JSONException) {
                        Log.e("LobbyFlow", "Update parse error", e)
                    }
                }
            }
            return@withContext lobbyId

        } catch (e: Exception) {
            Log.e("Debugging", "Error while creating lobby: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun sendLobbyJoin(playerName: String, lobbyID: String): LobbyResponseMessage? =
        withContext(Dispatchers.IO) {
            val session = getSession() ?: run {
                sendToMainThread("Not connected")
                return@withContext null
            }

            try {

                val joinRequest = LobbyRequestMessage(playerName)
                val json = gson.toJson(joinRequest)
                val destination = "/app/$lobbyID/join"

                val flow = session.subscribeText("/topic/$lobbyID")

                session.sendText(destination, json)
                sendToMainThread("Beitreten wird verarbeitet...")

                val payload = flow.first()
                val jsonResponse = JSONObject(payload)

                return@withContext LobbyResponseMessage(
                    lobbyId = lobbyID,
                    playerName = playerName,
                    isSuccessful = jsonResponse.getBoolean("successful"),
                    message = jsonResponse.getString("message")
                ).also {
                    scope.launch {
                        session.subscribeText("/topic/$lobbyID")
                            .collect { update ->
                                try {
                                    _lobbyUpdates.emit(
                                        gson.fromJson(
                                            update,
                                            LobbyResponseMessage::class.java
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("LobbyJoin", "Update error", e)
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyJoin", "Error", e)
                sendToMainThread("Fehler: ${e.message}")
                null
            }
        }

    /** Abonniert das Job-Topic, liefert **einmalig** die empfangenen Jobs im Callback. */
    fun subscribeJobs(
        gameId: Int,
        playerName: String,
        onJobs: (List<JobMessage>) -> Unit
    ) {
        getSession()?.let {
            scope.launch {
                try {
                    val dest = "/topic/$gameId/jobs/$playerName"
                    val rawMsg = session?.subscribeText(dest)?.first()
                    val jobs = gson.fromJson(rawMsg, Array<JobMessage>::class.java).toList()
                    sendToMainThread("📥 Jobs erhalten: ${jobs.joinToString(" + ") { it.title }}")
                    onJobs(jobs)

                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Subscriben: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }


    fun sendMove(player: String, action: String) {
        getSession()?.let {
            val message = StompMessage(playerName = player, action = action)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    session?.sendText("/app/move", json)
                    sendToMainThread("✅ Spielzug gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }

    fun sendRealMove(player: String, dice: Int) {
        getSession()?.let {
            val message = StompMessage(playerName = player, action = "$dice gewürfelt")
            val json = gson.toJson(message)
            scope.launch {
                try {
                    session?.sendText("/app/move", json)
                    callback("✅ Spielzug gesendet")
                } catch (e: Exception) {
                    callback("❌ Fehler beim Senden (move): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }

    fun sendChat(player: String, text: String) {
        getSession()?.let {
            val message = StompMessage(playerName = player, messageText = text)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    session?.sendText("/app/chat", json)
                    sendToMainThread("✅ Nachricht gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (chat): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }

    fun requestJobs(gameId: Int, playerName: String, hasDegree: Boolean) {
        getSession()?.let {
            val request = JobRequestMessage(
                playerName = playerName,
                gameId = gameId,
                hasDegree = hasDegree,
                jobId = null
            )

            val json = gson.toJson(request)

            scope.launch {
                try {
                    val destination = "/app/jobs/$gameId/$playerName/request"
                    session?.sendText(destination, json)
                    sendToMainThread("📨 Jobanfrage gesendet an $destination")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler bei Jobanfrage: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }

    fun selectJob(gameId: Int, playerName: String, job: JobMessage) {
        getSession()?.let {
            val json = gson.toJson(job)
            scope.launch {
                try {
                    val destination = "/app/jobs/$gameId/$playerName/select"
                    session?.sendText(destination, json)
                    // Direkte Textausgabe nach dem Senden
                    sendToMainThread("✅ Du hast Job „${job.title}“ (ID ${job.jobId}) ausgewählt")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden der Jobauswahl: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
        return
    }


    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
