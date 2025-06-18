package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.sdlapp.network.message.MoveMessage
import at.aau.serg.sdlapp.network.message.OutputMessage
import at.aau.serg.sdlapp.network.message.PlayerListMessage
import at.aau.serg.sdlapp.network.message.PlayerPositionsMessage
import at.aau.serg.sdlapp.network.message.StompMessage
import at.aau.serg.sdlapp.network.message.house.HouseBuyElseSellMessage
import at.aau.serg.sdlapp.network.message.house.HouseMessage
import at.aau.serg.sdlapp.network.message.job.JobMessage
import at.aau.serg.sdlapp.network.message.job.JobRequestMessage
import at.aau.serg.sdlapp.network.message.lobby.LobbyRequestMessage
import at.aau.serg.sdlapp.network.message.lobby.LobbyResponseMessage
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

//private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53217/websocket"
private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket"

//private const val WEBSOCKET_URI = "ws://192.168.8.140:8080/websocket-broker/websocket" //for testing
private const val NO_CONNECTION_MESSAGE = "Keine Verbindung aktiv"
private const val NO_CONNECTION_SUBSCRIPTION_MESSAGE =
    "❌ Verbindung nicht aktiv – Subscription fehlgeschlagen"


class StompConnectionManager(
    private val callback: (String) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    private var session: StompSession? = null
    val scope = CoroutineScope(ioDispatcher)
    private val gson = Gson()
    private val _lobbyUpdates = MutableSharedFlow<LobbyResponseMessage>()
    var isConnected: Boolean = false
    private val client = StompClient(OkHttpWebSocketClient())
    var onMoveReceived: ((MoveMessage) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null
    var onConnectionError: ((String) -> Unit)? = null
    var onPlayerListReceived: ((List<String>) -> Unit)? = null
    var onBoardDataReceived: ((List<at.aau.serg.sdlapp.model.board.Field>) -> Unit)? = null
    var onPlayerPositionsReceived: ((Map<String, Int>) -> Unit)? = null

    /**
     * Callback für Farbänderungen von Spielern
     * Wird aufgerufen, wenn ein Spieler seine Farbe ändert
     */
    var onPlayerColorChanged: ((playerId: String, color: String) -> Unit)? = null

    // Reconnect-Logik
    private var shouldReconnect = true
    private val maxReconnectAttempts = 5
    private var reconnectAttempts = 0
    /**
     * Gibt die aktuelle Session zurück, falls verbunden, sonst null.
     */
    val sessionOrNull: StompSession?
        get() = synchronized(this) {
            if (isConnected) session else null
        }

    suspend fun connect(playerName: String): Boolean = withContext(ioDispatcher) {
        try {
            session = client.connect(WEBSOCKET_URI, login = playerName)
            isConnected = true
            launchMessageCollectors()
            if(checkSuccessfulRegistration(playerName)){
                sendToMainThread("✅ Verbunden mit Server")
                onConnectionStateChanged?.invoke(true)
                return@withContext true
            }
            isConnected = false
            onConnectionStateChanged?.invoke(false)
            return@withContext false
        } catch (e: Exception) {
            sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            isConnected = false
            onConnectionStateChanged?.invoke(false)
            return@withContext false
        }
    }

    fun connectAsync(playerName: String): kotlinx.coroutines.CompletableDeferred<Boolean> {
        val result = kotlinx.coroutines.CompletableDeferred<Boolean>()
        scope.launch {
            result.complete(connect(playerName))
        }
        return result
    }

    suspend fun checkSuccessfulRegistration(playerName: String) : Boolean = withContext(ioDispatcher) {
        val message = LobbyRequestMessage(playerName)
        val json = gson.toJson(message)
        Log.d("Debugging", "Session: $sessionOrNull, Player: $playerName")
        val currentSession = sessionOrNull
        if (currentSession != null) {
            // Zuerst Collector starten
            val collector = scope.async {
                launchPlayerCheckCollector(playerName)
            }
            
            // Dann die Nachricht senden
            delay(100) // Kurze Verzögerung um sicherzustellen, dass der Collector aktiv ist
            Log.d("Debugging", "Sending check request to /app/players/check")
            sessionOrNull?.sendText("/app/players/check", json)
            
            // Auf das Ergebnis warten
            return@withContext collector.await()
        } else {
            sendToMainThread(NO_CONNECTION_MESSAGE)
            return@withContext false
        }
    }

    private suspend fun launchPlayerCheckCollector(playerName: String) : Boolean = withContext(ioDispatcher) {
        var result = false
        var parsedPlayer = ""
        Log.d("Debugging", "Starting player check collector for: $playerName")
        
        try {
            sessionOrNull?.let { s->
                withContext(ioDispatcher) {
                    try {
                        val subscription = s.subscribeText("/user/queue/player/check")
                        Log.d("PlayerCheck", "Subscribed to /user/queue/player/check")
                        
                        try {
                            withTimeout(10000) { // 10 Sekunden Timeout
                                try {
                                    var found = false
                                    subscription.collect { msg ->
                                        if (!found) { // Verarbeite nur wenn noch nicht gefunden
                                            Log.d("PlayerCheck", "✅ Received player check response: $msg")
                                            
                                            val json = JSONObject(msg)
                                            val wasSuccessful = json.getBoolean("wasSuccessful")
                                            parsedPlayer = json.getString("playerName")
                                            
                                            // Wenn die Nachricht für diesen Spieler ist
                                            if (parsedPlayer == playerName) {
                                                Log.d("PlayerCheck", "Found matching response for $playerName: $wasSuccessful")
                                                result = wasSuccessful
                                                if (!result) {
                                                    Log.d("PlayerCheck", "⚠️ Player check failed for $playerName")
                                                    sendToMainThread("Spieler-Check fehlgeschlagen")
                                                }
                                                found = true
                                                throw CancellationException("Message found", null)// Beende den Flow aktiv
                                            } else {
                                                Log.d("PlayerCheck", "Ignoring response for different player: $parsedPlayer")
                                            }
                                        }
                                    }
                                } catch (e: CancellationException) {
                                    if (e.message == "Message found") {
                                        Log.d("PlayerCheck", "Successfully completed player check")
                                    } else {
                                        throw e
                                    }
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            if (!result) { // Nur als Fehler loggen wenn wir keine erfolgreiche Antwort hatten
                                Log.e("PlayerCheck", "⏰ Check timed out for player: $playerName")
                                sendToMainThread("Zeitüberschreitung beim Spieler-Check")
                                return@withContext false
                            }
                        } catch (e: Exception) {
                            Log.e("PlayerCheck", "❌ Error processing check response", e)
                            sendToMainThread("Fehler beim Verarbeiten der Antwort: ${e.message}")
                            return@withContext false
                        }
                    } catch (e: Exception) {
                        Log.e("PlayerCheck", "❌ General error during check", e)
                        sendToMainThread("Fehler beim Spieler-Check: ${e.message}")
                        return@withContext false
                    }
                }
            } ?: run {
                Log.e("PlayerCheck", "No active session for player check")
                sendToMainThread("Keine aktive Verbindung für Spieler-Check")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("PlayerCheck", "Fatal error in player check", e)
            sendToMainThread("Schwerwiegender Fehler beim Spieler-Check")
            return@withContext false
        }

        return@withContext result
    }

    private fun launchMessageCollectors() {
        sessionOrNull?.let { s ->
            scope.launch {
                s.subscribeText("/topic/game").collect { msg ->
                    handleGameMessage(msg)
                }
            }
            scope.launch {
                s.subscribeText("/topic/chat").collect { msg ->
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        sendToMainThread("❌ Fehler beim Verarbeiten der Chat-Nachricht: ${e.message}")
                    }
                }
            }
            scope.launch {
                s.subscribeText("/topic/board/data").collect { msg ->
                    handleBoardDataMessage(msg)
                }
            }
            scope.launch {
                s.subscribeText("/topic/players/positions").collect { msg ->
                    handlePlayerPositionsMessage(msg)
                }
            }
            scope.launch {
                s.subscribeText("/topic/player/colors").collect { msg ->
                    handlePlayerColorMessage(msg)
                }
            }
        }
    }

    private fun handleGameMessage(msg: String) {
        try {
            sendToMainThread("📥 Nachricht vom Server empfangen: ${msg.take(100)}${if (msg.length > 100) "..." else ""}")

            if (msg.contains("\"type\":\"players\"") || msg.contains("\"playerList\":[")) {
                try {
                    val playerListResponse = gson.fromJson(msg, PlayerListMessage::class.java)
                    sendToMainThread("👥 Spielerliste empfangen: ${playerListResponse.playerList.joinToString()}")
                    onPlayerListReceived?.invoke(playerListResponse.playerList)
                    return
                } catch (e: Exception) {
                    sendToMainThread("⚠️ Fehler beim Parsen der Spielerliste: ${e.message}")
                }
            }

            try {
                val moveMessage = gson.fromJson(msg, MoveMessage::class.java)
                if (moveMessage.fieldIndex >= 0) {
                    sendToMainThread("🚗 MOVE ERKANNT: Spieler ${moveMessage.playerName} bewegt zu Feld ${moveMessage.fieldIndex}")
                    sendToMainThread("🔢 Details: typ=${moveMessage.typeString}, nächste Felder=${moveMessage.nextPossibleFields}")
                    onMoveReceived?.invoke(moveMessage)
                    return
                }
            } catch (e: Exception) {
                sendToMainThread("⚠️ Nachricht ist keine gültige MoveMessage: ${e.message}")
            }

            try {
                val output = gson.fromJson(msg, OutputMessage::class.java)
                sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
            } catch (e: Exception) {
                sendToMainThread("⚠️ Nachricht ist auch keine OutputMessage: ${e.message}")
            }
        } catch (e: Exception) {
            sendToMainThread("⚠️ Fehler beim Verarbeiten einer Game-Nachricht: ${e.message}")
            sendToMainThread("⚠️ Nachricht war: $msg")
            try {
                val output = gson.fromJson(msg, OutputMessage::class.java)
                sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
            } catch (innerEx: Exception) {
                sendToMainThread("❌ Fehler beim Parsen der Nachricht: ${innerEx.message}")
            }
        }
    }

    private fun handleBoardDataMessage(msg: String) {
        try {
            sendToMainThread("📊 Board-Daten vom Server empfangen")
            val boardDataMessage = gson.fromJson(msg, BoardDataMessage::class.java)

            // Konvertiere FieldDto zu lokalen Field-Objekten
            val fields = boardDataMessage.fields.map { it.toField() }

            // Rufe den Callback mit den Board-Daten auf
            onBoardDataReceived?.invoke(fields)
        } catch (e: Exception) {
            sendToMainThread("⚠️ Fehler beim Verarbeiten der Board-Daten: ${e.message}")
        }
    }

    private fun handlePlayerPositionsMessage(msg: String) {
        try {
            sendToMainThread("👥 Spielerpositionen vom Server empfangen")
            val positionsMessage = gson.fromJson(msg, PlayerPositionsMessage::class.java)

            // Log erhaltene Positionen
            val count = positionsMessage.playerPositions.size
            sendToMainThread("📍 $count Spielerpositionen aktualisiert")

            // Rufe den Callback mit den Spielerpositionen auf
            onPlayerPositionsReceived?.invoke(positionsMessage.playerPositions)
        } catch (e: Exception) {
            sendToMainThread("⚠️ Fehler beim Verarbeiten der Spielerpositionen: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Verarbeitet eine Nachricht über die Farbänderung eines Spielers
     */
    private fun handlePlayerColorMessage(msg: String) {
        try {
            val stompMessage = gson.fromJson(msg, StompMessage::class.java)

            // Prüfe, ob es sich um eine color_selected-Nachricht handelt
            if (stompMessage.action?.startsWith("color_selected:") == true) {
                val color = stompMessage.action.substring("color_selected:".length)
                val playerId = stompMessage.playerName

                sendToMainThread("🎨 Spieler $playerId hat Farbe $color ausgewählt")

                // Rufe den Callback für Farbänderungen auf
                onPlayerColorChanged?.invoke(playerId, color)
            }
        } catch (e: Exception) {
            sendToMainThread("⚠️ Fehler beim Verarbeiten der Farbänderung: ${e.message}")
        }
    }    fun sendGameStart(gameId: Int, playerName: String) {
        sessionOrNull?.let {
            scope.launch {
                try {
                    it.sendText("/app/game/start/$gameId", "")
                    sendToMainThread("📨 Spielstart gesendet, Player=$playerName")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden des Spielstarts: \\${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_MESSAGE)
    }    suspend fun sendLobbyLeave(playerName: String, lobbyID: String) {
        val currentSession = sessionOrNull ?: run {
            sendToMainThread(NO_CONNECTION_MESSAGE)
            return
        }
        try {
            val request = LobbyRequestMessage(playerName)
            val json = gson.toJson(request)
            currentSession.sendText("/app/$lobbyID/leave", json)
            sendToMainThread("Lobby wird verlassen")

        } catch (e: Exception) {
            Log.e("Lobby Error", "Error while leaving lobby: ${e.message}")
        }
    }    suspend fun sendLobbyCreate(playerName: String): String? = withContext(ioDispatcher) {
        val currentSession: StompSession = sessionOrNull ?: run {
            sendToMainThread(NO_CONNECTION_MESSAGE)
            return@withContext null
        }
        try {
            val request = LobbyRequestMessage(playerName)
            val json = gson.toJson(request)
            val flow = sessionOrNull?.subscribeText("/user/queue/lobby/created")
            sessionOrNull?.sendText("/app/lobby/create", json)
            sendToMainThread("Lobby wird erstellt")
            val response = flow?.first()
            val lobbyId = JSONObject(response).getString("lobbyID")
            scope.launch {
                val updateFlow = sessionOrNull?.subscribeText("/topic/$lobbyId")
                updateFlow?.collect { payload ->
                    try {
                        _lobbyUpdates.emit(gson.fromJson(payload, LobbyResponseMessage::class.java))
                    } catch (e: JSONException) {
                        Log.e("LobbyFlow", "Update parse error", e)
                    }
                }
            }
            lobbyId
        } catch (e: Exception) {
            Log.e("Debugging", "Error while creating lobby: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun sendLobbyJoin(playerName: String, lobbyID: String): LobbyResponseMessage? =
        withContext(ioDispatcher) {
            val session = sessionOrNull ?: run {
                sendToMainThread(NO_CONNECTION_MESSAGE)
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
                scope.launch {
                    session.subscribeText("/topic/$lobbyID").collect { update ->
                        try {
                            _lobbyUpdates.emit(
                                gson.fromJson(
                                    update, LobbyResponseMessage::class.java
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("LobbyJoin", "Update error", e)
                        }
                    }
                }
                LobbyResponseMessage(
                    lobbyId = lobbyID,
                    playerName = playerName,
                    isSuccessful = jsonResponse.getBoolean("successful"),
                    message = jsonResponse.getString("message")
                )
            } catch (e: Exception) {
                Log.e("LobbyJoin", "Error", e)
                sendToMainThread("Fehler: ${e.message}")
                null
            }
        }

    fun subscribeJobs(
        gameId: Int, playerName: String, onJobs: (List<JobMessage>) -> Unit
    ) {
        sessionOrNull?.let {
            scope.launch {
                try {
                    val dest = "/topic/$gameId/jobs/$playerName"
                    // Warte auf genau eine Nachricht mit den beiden Jobs
                    val rawMsg = sessionOrNull?.subscribeText(dest)?.first()
                    val jobs = gson.fromJson(rawMsg, Array<JobMessage>::class.java).toList()
                    // Callback auf Main-Thread
                    withContext(mainDispatcher) {
                        onJobs(jobs)
                    }
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Subscriben: \\${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun sendMove(player: String, action: String) {
        sessionOrNull?.let {
            val message = StompMessage(playerName = player, action = action)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    sessionOrNull?.sendText("/app/move", json)
                    sendToMainThread("✅ Spielzug gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun sendRealMove(player: String, dice: Int, currentFieldIndex: Int = -1) {
        sessionOrNull?.let {
            val moveInfo =
                if (currentFieldIndex >= 0) "$dice gewürfelt:$currentFieldIndex" else "$dice gewürfelt"
            val message = StompMessage(playerName = player, action = moveInfo, gameId = player)
            val json = gson.toJson(message)
            sendToMainThread("🎲 Sende Würfelzug $dice von Feld $currentFieldIndex")
            scope.launch {
                try {
                    sessionOrNull?.sendText("/app/move", json)
                    sendToMainThread("✅ Spielzug gesendet (von Feld $currentFieldIndex)")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden des Spielzugs: ${e.message}")
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    handleReconnect()
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun sendChat(player: String, text: String) {
        sessionOrNull?.let {
            val message = StompMessage(playerName = player, messageText = text)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    sessionOrNull?.sendText("/app/chat", json)
                    sendToMainThread("✅ Nachricht gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (chat): ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    /**
     * Fordert beim Backend an, für das gegebene Spiel (gameId) das Job-Repository anzulegen.
    Später soll das Repo durch den Screen Change von Lobby zu Game im Backend direkt ohne Aufruf erzeugt werden
     */
    fun requestJobRepository(gameId: Int) {
        sessionOrNull?.let {
            scope.launch {
                try {
                    // Leere Nachricht an diesen STOMP-Endpunkt
                    sessionOrNull?.sendText("/app/game/createJobRepo/$gameId", "")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfordern des Job-Repos: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_MESSAGE)
    }


    fun requestJobs(gameId: Int, playerName: String) {
        sessionOrNull?.let {
            val request = JobRequestMessage(
                playerName = playerName, gameId = gameId, jobId = null
            )
            val json = gson.toJson(request)
            scope.launch {
                try {
                    val destination = "/app/jobs/$gameId/$playerName/request"
                    sessionOrNull?.sendText(destination, json)

                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler bei Jobanfrage: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun selectJob(gameId: Int, playerName: String, job: JobMessage) {
        sessionOrNull?.let {
            val json = gson.toJson(job)
            scope.launch {
                try {
                    val destination = "/app/jobs/$gameId/$playerName/select"
                    sessionOrNull?.sendText(destination, json)
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden der Jobauswahl: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun requestActivePlayers(player: String) {
        sessionOrNull?.let {
            val message =
                StompMessage(playerName = player, action = "get-all-players", gameId = player)
            val json = gson.toJson(message)
            sendToMainThread("👥 Frage aktive Spieler ab...")
            scope.launch {
                try {
                    sessionOrNull?.sendText("/app/move", json)
                    sendToMainThread("✅ Anfrage für Spielerliste gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfragen der Spielerliste: ${e.message}")
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    handleReconnect()
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv bei Spielerabfrage!")
    }

    /**
     * Sendet eine Nachricht an das angegebene Ziel
     *
     * @param destination Das Ziel, an das die Nachricht gesendet werden soll
     * @param payload Der Inhalt der Nachricht (als JSON-String oder einfacher String)
     */
    fun sendMessage(destination: String, payload: String) {
        sessionOrNull?.let {
            scope.launch {
                try {
                    sessionOrNull?.sendText(destination, payload)
                    sendToMainThread("✅ Nachricht an $destination gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden an $destination: ${e.message}")
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    handleReconnect()
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Senden fehlgeschlagen")
    }

    private fun sendToMainThread(message: String) {
        Handler(Looper.getMainLooper()).post {
            if (!message.startsWith("📥") || message.length < 100) {
                callback(message)
            } else {
                callback("${message.substring(0, 100)}...")
                if (message.contains("\"type\":\"players\"") || message.contains("\"playerList\":[")) {
                    callback("👥 DEBUG: Spielerliste im Nachrichteninhalt gefunden")
                }
            }
        }
    }

    private fun handleReconnect() {
        if (!shouldReconnect || reconnectAttempts >= maxReconnectAttempts) {
            sendToMainThread("❌ Maximale Anzahl an Wiederverbindungsversuchen erreicht")
            onConnectionError?.invoke("Verbindung zum Server verloren")
            return
        }
        reconnectAttempts++
        scope.launch {
            sendToMainThread("🔄 Versuche erneut zu verbinden (Versuch $reconnectAttempts/$maxReconnectAttempts)")
            delay(1000L * reconnectAttempts)
            try {
                connect("")
            } catch (e: Exception) {
                sendToMainThread("❌ Wiederverbindung fehlgeschlagen: ${e.message}")
                handleReconnect()
            }
        }
    }

    fun disconnect() {
        shouldReconnect = false
        scope.launch {
            try {
                sessionOrNull?.disconnect()
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                sendToMainThread("✓ Verbindung zum Server getrennt")
            } catch (e: Exception) {
                sendToMainThread("⚠️ Fehler beim Trennen der Verbindung: ${e.message}")
            }
        }
    }

    fun requestHouseRepository(gameId: Int) {
        println(">> requestHouseRepository(gameId=$gameId) aufgerufen")
        sessionOrNull?.let {
            scope.launch {
                try {
                    println("   → Sende leere Nachricht an /app/game/createHouseRepo/$gameId")
                    sessionOrNull?.sendText("/app/game/createHouseRepo/$gameId", "")
                    println("   ✓ Anfrage erfolgreich gesendet")
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Anfordern des House-Repos: ${e.message}")
                }
            }
        } ?: run {
            println("   ✗ Keine aktive Session – Repository-Anfrage nicht gesendet")
            sendToMainThread(NO_CONNECTION_MESSAGE)
        }
    }

    fun subscribeHouses(
        gameId: Int, playerName: String, onHouses: (List<HouseMessage>) -> Unit
    ) {
        val dest = "/topic/$gameId/houses/$playerName/options"
        println(">> subscribeHouses: subscribe to $dest")
        sessionOrNull?.let {
            scope.launch {
                try {
                    val rawMsg = sessionOrNull?.subscribeText(dest)?.first()
                    println("   ← rawMsg: $rawMsg")
                    val houses = gson.fromJson(rawMsg, Array<HouseMessage>::class.java).toList()
                    println("   ✓ Parsed houses: ${houses.map { it.bezeichnung }}")
                    withContext(mainDispatcher) {
                        onHouses(houses)
                    }
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Subscriben (Häuser): ${e.message}")
                    sendToMainThread("❌ Fehler beim Subscriben (Häuser): ${e.message}")
                }
            }
        } ?: run {
            println("   ✗ Keine aktive Session – Subscription fehlgeschlagen")
            sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
        }
    }


    fun buyHouse(gameId: Int, playerName: String) {
        println(">> buyHouse(gameId=$gameId, playerName=$playerName) aufgerufen")
        sessionOrNull?.let {
            val message =
                HouseBuyElseSellMessage(playerID = playerName, gameId = gameId, buyElseSell = true)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    val destination = "/app/houses/$gameId/$playerName/choose"
                    println("   → Sende Kaufanfrage an $destination mit Payload: $json")
                    sessionOrNull?.sendText(destination, json)
                    println("   ✓ Kaufanfrage gesendet")
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Senden der Kaufanfrage: ${e.message}")
                    sendToMainThread("❌ Fehler beim Senden der Kaufanfrage: ${e.message}")
                }
            }
        } ?: run {
            println("   ✗ Keine aktive Session – Kaufanfrage fehlgeschlagen")
            sendToMainThread("❌ Verbindung nicht aktiv – Kaufanfrage fehlgeschlagen")
        }
    }

    fun sellHouse(gameId: Int, playerName: String) {
        println(">> sellHouse(gameId=$gameId, playerName=$playerName) aufgerufen")
        sessionOrNull?.let {
            val message =
                HouseBuyElseSellMessage(playerID = playerName, gameId = gameId, buyElseSell = false)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    val destination = "/app/houses/$gameId/$playerName/choose"
                    println("   → Sende Verkaufsanfrage an $destination mit Payload: $json")
                    sessionOrNull?.sendText(destination, json)
                    println("   ✓ Verkaufsanfrage gesendet")
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Senden der Verkaufsanfrage: ${e.message}")
                    sendToMainThread("❌ Fehler beim Senden der Verkaufsanfrage: ${e.message}")
                }
            }
        } ?: run {
            println("   ✗ Keine aktive Session – Verkaufsanfrage fehlgeschlagen")
            sendToMainThread("❌ Verbindung nicht aktiv – Verkaufsanfrage fehlgeschlagen")
        }
    }

    fun finalizeHouseAction(gameId: Int, playerName: String, house: HouseMessage) {
        println(">> finalizeHouseAction(gameId=$gameId, playerName=$playerName, houseId=${house.houseId}) aufgerufen")
        sessionOrNull?.let {
            val json = gson.toJson(house)
            scope.launch {
                try {
                    val destination = "/app/houses/$gameId/$playerName/finalize"
                    println("   → Sende Finalisierungsanfrage an $destination mit Payload: $json")
                    sessionOrNull?.sendText(destination, json)
                    println("   ✓ Finalisierungsanfrage gesendet")
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Senden der Finalisierungsanfrage: ${e.message}")
                    sendToMainThread("❌ Fehler beim Senden der Finalisierungsanfrage: ${e.message}")
                }
            }
        } ?: run {
            println("   ✗ Keine aktive Session – Finalisierungsanfrage fehlgeschlagen")
            sendToMainThread("❌ Verbindung nicht aktiv – Finalisierungsanfrage fehlgeschlagen")
        }
    }

    /**
     * Lauscht genau auf die Bestätigung (ein einzelnes HouseMessage),
     * die das Backend nach finalizeHouseAction sendet.
     */
    fun subscribeHouseConfirmation(
        gameId: Int, playerName: String, onConfirm: (HouseMessage) -> Unit
    ) {
        val dest = "/topic/$gameId/houses/$playerName/confirmation"
        println(">> subscribeHouseConfirmation: subscribe to $dest")
        sessionOrNull?.let {
            scope.launch {
                try {
                    // warte auf genau eine Nachricht
                    val raw = sessionOrNull?.subscribeText(dest)?.first()
                    println("   ← Confirmation raw: $raw")
                    if (raw != null) {
                        // parse single HouseMessage
                        val house = gson.fromJson(raw, HouseMessage::class.java)
                        println("   ✓ Parsed confirmation: ${house.bezeichnung} (taken=${house.isTaken})")
                        withContext(mainDispatcher) {
                            onConfirm(house)
                        }
                    } else {
                        println("   ✗ confirmation payload null")
                    }
                } catch (e: Exception) {
                    println("   ✗ Fehler beim Subscriben (Confirmation): ${e.message}")
                }
            }
        } ?: println("   ✗ Keine aktive Session – Confirmation-Subscription fehlgeschlagen")
    }

    /**
     * Fordert die aktuellen Positionen aller Spieler vom Server an
     */
    fun requestPlayerPositions() {
        sessionOrNull?.let {
            scope.launch {
                try {
                    sendToMainThread("📍 Fordere Spielerpositionen vom Server an")
                    sessionOrNull?.sendText("/app/players/positions/request", "{}")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfordern der Spielerpositionen: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_MESSAGE)
    }

    /**
     * Sendet eine Anfrage zum Beitreten eines bestehenden Spiels
     *
     * @param lobbyId Die ID der Lobby, zu der das Spiel gehört
     * @param playerName Der Name des Spielers, der beitreten möchte
     */
    /**
     * Tritt einem existierenden Spiel bei.
     * Sendet eine Beitrittsanfrage an den Server und abonniert relevante Topics.
     *
     * @param lobbyId Die ID der Lobby
     * @param playerName Der Name des Spielers, der beitreten möchte
     */
    fun joinExistingGame(lobbyId: String, playerName: String) {
        sessionOrNull?.let {
            scope.launch {
                try {
                    // 1. Lobby-Topic abonnieren für Updates
                    scope.launch {
                        try {
                            sessionOrNull?.subscribeText("/topic/$lobbyId")?.collect { message ->
                                sendToMainThread("📩 Nachricht aus Lobby $lobbyId: $message")
                            }
                        } catch (e: Exception) {
                            sendToMainThread("❌ Fehler beim Abonnieren des Lobby-Topics: ${e.message}")
                        }
                    }
                    sendToMainThread("✅ Lobby-Topic abonniert: /topic/$lobbyId")

                    // 2. Game-Status Topic abonnieren
                    scope.launch {
                        try {
                            sessionOrNull?.subscribeText("/topic/game/$lobbyId/status")
                                ?.collect { message ->
                                    sendToMainThread("📢 Spielstatus: $message")
                                }
                        } catch (e: Exception) {
                            sendToMainThread("❌ Fehler beim Abonnieren des Status-Topics: ${e.message}")
                        }
                    }
                    sendToMainThread("✅ Spiel-Status-Topic abonniert: /topic/game/$lobbyId/status")

                    // 3. Beitrittsanfrage senden
                    val message = StompMessage(playerName = playerName, gameId = lobbyId)
                    val json = gson.toJson(message)
                    sessionOrNull?.sendText("/app/game/$lobbyId/join", json)
                    sendToMainThread("✅ Beitrittsanfrage für Spiel in Lobby $lobbyId gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden der Beitrittsanfrage: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_MESSAGE)
    }

    /**
     * Subscribes to the game status topic for a specific lobby to receive game start notifications
     */
    fun subscribeToGameStatus(gameId: String, onGameStarted: () -> Unit) {
        sessionOrNull?.let { session ->
            scope.launch {
                try {
                    Log.d(
                        "StompConnectionManager",
                        "Subscribing to game status: /topic/game/$gameId/status"
                    )
                    session.subscribeText("/topic/game/$gameId/status").collect { msg ->
                        Log.d("StompConnectionManager", "🎲 Game status message received: $msg")
                        if (msg.contains("Spiel wurde gestartet")) {
                            Log.d("StompConnectionManager", "🎮 Game started notification received!")
                            withContext(mainDispatcher) {
                                onGameStarted()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StompConnectionManager", "❌ Error subscribing to game status", e)
                }
            }
        } ?: Log.e("StompConnectionManager", NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }

    fun sendColorSelection(player: String, color: String) {
        sessionOrNull?.let {
            val message = StompMessage(playerName = player, action = "color:$color")
            val json = gson.toJson(message)
            scope.launch {
                try {
                    sessionOrNull?.sendText("/app/player/color", json)
                    sendToMainThread("🎨 Farbauswahl '$color' gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden der Farbauswahl: ${e.message}")
                }
            }
        } ?: sendToMainThread(NO_CONNECTION_SUBSCRIPTION_MESSAGE)
    }
}

/**
 * Interface für die Move-Callbacks
 */
fun interface MoveCallbacks {
    fun onPlayersChanged()
}