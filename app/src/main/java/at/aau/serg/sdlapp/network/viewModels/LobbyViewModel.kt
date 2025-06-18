package at.aau.serg.sdlapp.network.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.json.JSONObject
import java.time.LocalDateTime

class LobbyViewModel(
    private val session: StompSession
) : ViewModel() {
    private val _players = MutableStateFlow<List<String>>(emptyList())
    val players: StateFlow<List<String>> get() = _players.asStateFlow()

    // StateFlow für den Spielstatus
    private val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> get() = _isGameStarted.asStateFlow()

    private var updatesJob: Job? = null
    private var currentLobbyId: String? = null

    // Flow für die Lobby-Updates
    private var lobbySubscription: kotlinx.coroutines.flow.Flow<String>? = null
    private var gameStartSubscription: kotlinx.coroutines.flow.Flow<String>? = null


    private suspend fun setupLobbySubscription(lobbyId: String) {
        try {
            val topicPath = "/topic/lobby/$lobbyId"
            lobbySubscription = session.subscribeText(topicPath)
        } catch (e: Exception) {
            Log.e("LobbyViewModel", "Error setting up lobby subscription", e)
        }
    }

    private suspend fun setupGameStartSubscription(lobbyID: String) {
        try {
            val gameStartPath = "/topic/game/$lobbyID/status"
            gameStartSubscription = session.subscribeText(gameStartPath)
        } catch (e: Exception) {
            Log.e("LobbyViewModel", "Error setting up game start subscription", e)
        }
    }

    fun initialize(lobbyId: String, currentPlayer: String) {
        currentLobbyId = lobbyId
        _players.value = emptyList()

        viewModelScope.launch {
            try {
                setupLobbySubscription(lobbyId)
                setupGameStartSubscription(lobbyId)
                startObserving(lobbyId)
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "❌ Error in initialize", e)
            }
        }
    }

    private fun startObserving(lobbyId: String) {
        updatesJob?.cancel()
        Log.d("LobbyViewModel", "Started Observing Lobby updates for lobby $lobbyId")
        updatesJob = viewModelScope.launch {
            try {
                Log.d("LobbyViewModel", "Starting message collection at ${LocalDateTime.now()}")

                // Haupt-Topic für Lobby-Updates
                launch {
                    try {
                        val subscription = lobbySubscription
                        if (subscription == null) {
                            Log.e("LobbyViewModel", "No subscription available for collecting")
                            return@launch
                        }

                        subscription.collect { payload ->
                            Log.d("LobbyViewModel", "Raw message received: $payload")

                            if (payload.isBlank()) {
                                Log.w("LobbyViewModel", "⚠️ Received empty payload")
                                return@collect
                            }

                            try {
                                val json = JSONObject(payload)

                                when {
                                    // LobbyUpdateMessage Format (vom Server)
                                    json.has("player1") -> {
                                        val players = listOfNotNull(
                                            json.optString("player1").takeIf { it.isNotBlank() },
                                            json.optString("player2").takeIf { it.isNotBlank() },
                                            json.optString("player3").takeIf { it.isNotBlank() },
                                            json.optString("player4").takeIf { it.isNotBlank() }
                                        )
                                        Log.d("LobbyViewModel", "Player list from update: $players")
                                        _players.value = players

                                        // Überprüfe Spielstart
                                        if (json.has("started")) {
                                            val isStarted = json.getBoolean("started")
                                            Log.d(
                                                "LobbyViewModel",
                                                "🎮 Game started status: $isStarted"
                                            )
                                            if (isStarted && !_isGameStarted.value) {
                                                Log.d("LobbyViewModel", "🎯 Game is now started!")
                                                _isGameStarted.value = true
                                            }
                                        }
                                    }
                                    // Einzelner Spieler Update
                                    json.has("playerName") -> {
                                        Log.d("LobbyViewModel", "👤 Processing player update")
                                        val playerName = json.getString("playerName")
                                        if (playerName.isNotBlank()) {
                                            _players.update { currentPlayers ->
                                                if (!currentPlayers.contains(playerName)) {
                                                    Log.d(
                                                        "LobbyViewModel",
                                                        "➕ Adding new player: $playerName"
                                                    )
                                                    currentPlayers + playerName
                                                } else {
                                                    currentPlayers
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        Log.w(
                                            "LobbyViewModel",
                                            "⚠️ Unknown message format: $payload"
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("LobbyViewModel", "❌ Error processing message", e)
                                Log.e("LobbyViewModel", "Payload was: $payload")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LobbyViewModel", "❌ Error collecting messages", e)
                    }
                }

                // Separates Topic für Spiel-Status
                launch {
                    try {
                        val statusTopic = "/topic/game/$lobbyId/status"
                        Log.d("LobbyViewModel", "📥 Subscribing to game status: $statusTopic")
                        session.subscribeText(statusTopic).collect { msg ->
                            Log.d("LobbyViewModel", "📨 Received on $statusTopic: $msg")
                            if (msg.contains("Spiel wurde gestartet")) {
                                Log.d("LobbyViewModel", "🎮 Game start notification received")
                                _isGameStarted.value = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LobbyViewModel", "❌ Error in game status subscription", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "❌ Fatal error in startObserving", e)
                Log.e("LobbyViewModel", "Stack trace:", e)
            }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            try {
                val lobbyId = currentLobbyId ?: run {
                    Log.e("LobbyViewModel", "❌ Kann Spiel nicht starten: Keine Lobby-ID gesetzt")
                    return@launch
                }

                Log.d("LobbyViewModel", "🎮 Sende Spielstart-Anfrage an Server für Lobby $lobbyId")
                session.sendText("/app/game/start/$lobbyId", "")
                Log.d(
                    "LobbyViewModel",
                    "✅ Spielstart-Anfrage erfolgreich gesendet, warte auf Bestätigung..."
                )

                // Kurz warten und dann prüfen, ob das isGameStarted-Flag gesetzt wurde
                // Dies ist ein Workaround falls die Server-Aktualisierung nicht korrekt ankommt
                launch {
                    delay(1000) // 1 Sekunde warten
                    if (!_isGameStarted.value) {
                        Log.d(
                            "LobbyViewModel",
                            "⏱️ Nach 1 Sekunde noch kein Spielstart erkannt, prüfe erneut..."
                        )
                        // Optional: hier könnte man eine direkte Anfrage nach dem Spielstatus stellen
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "❌ Fehler beim Starten des Spiels", e)
            }
        }
    }

    fun forceTriggerGameStart() {
        if (!_isGameStarted.value) {
            Log.d("LobbyViewModel", "⚠️ Erzwinge Spielstart (Fallback-Mechanismus)")
            _isGameStarted.value = true
        }
    }
}



