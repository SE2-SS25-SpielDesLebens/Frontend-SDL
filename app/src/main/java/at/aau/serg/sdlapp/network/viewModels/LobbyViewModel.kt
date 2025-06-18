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

    fun initialize(lobbyId: String, currentPlayer: String) {
        currentLobbyId = lobbyId
        // Setze die Spielerliste initial leer, damit das erste LobbyUpdateMessage die Liste korrekt setzt
        _players.value = emptyList()
        startObserving(lobbyId)

        // Zusätzlich auf direkte Game-Status-Nachrichten lauschen

        viewModelScope.launch {
            try {
                Log.d(
                    "LobbyViewModel",
                    "Subscribing to additional game status topic: /topic/game/$lobbyId/status"
                )
                session.subscribeText("/topic/game/$lobbyId/status").collect { msg ->
                    Log.d("LobbyViewModel", "🎲 Game status message received: $msg")
                    if (msg.contains("Spiel wurde gestartet")) {
                        Log.d("LobbyViewModel", "🎮 Direct game started notification received!")
                        _isGameStarted.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "❌ Error subscribing to game status", e)

            }
        }
    }

    private fun startObserving(lobbyId: String) {
        updatesJob?.cancel()
        Log.d("LobbyViewModel", "🔄 Started Observing Lobby updates for lobby $lobbyId")
        
        // Überprüfe Session-Status
        if (session == null) {
            Log.e("LobbyViewModel", "❌ Session ist null beim Start des Observings")
            return
        }
        
        updatesJob = viewModelScope.launch {
            try {
                Log.d("LobbyViewModel", "📥 Setting up subscriptions")
                
                // Haupt-Topic für Lobby-Updates
                launch {
                    try {
                        val topicPath = "/topic/$lobbyId"
                        Log.d("LobbyViewModel", "📥 Subscribing to main lobby topic: $topicPath")
                        
                        session.subscribeText(topicPath).collect { payload ->
                            Log.d("LobbyViewModel", "📨 Raw message received: $payload")
                            
                            if (payload.isNullOrBlank()) {
                                Log.w("LobbyViewModel", "⚠️ Received empty payload")
                                return@collect
                            }
                            
                            try {
                                val json = JSONObject(payload)
                                Log.d("LobbyViewModel", "✅ Parsed JSON: $json")
                                
                                when {
                                    // LobbyUpdateMessage Format (vom Server)
                                    json.has("player1") -> {
                                        Log.d("LobbyViewModel", "📝 Processing LobbyUpdateMessage")
                                        try {
                                            val players = listOfNotNull(
                                                json.optString("player1").takeIf { it.isNotBlank() },
                                                json.optString("player2").takeIf { it.isNotBlank() },
                                                json.optString("player3").takeIf { it.isNotBlank() },
                                                json.optString("player4").takeIf { it.isNotBlank() }
                                            )
                                            Log.d("LobbyViewModel", "👥 Player list from update: $players")
                                            _players.value = players
                                            
                                            // Überprüfe Spielstart
                                            if (json.has("isStarted")) {
                                                val isStarted = json.getBoolean("isStarted")
                                                Log.d("LobbyViewModel", "🎮 Game started status: $isStarted")
                                                if (isStarted && !_isGameStarted.value) {
                                                    Log.d("LobbyViewModel", "🎯 Game is now started!")
                                                    _isGameStarted.value = true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("LobbyViewModel", "❌ Error processing player list: ${e.message}")
                                        }
                                    }
                                    // Einzelner Spieler Update
                                    json.has("playerName") -> {
                                        Log.d("LobbyViewModel", "👤 Processing player update")
                                        val playerName = json.getString("playerName")
                                        if (playerName.isNotBlank()) {
                                            _players.update { currentPlayers ->
                                                if (!currentPlayers.contains(playerName)) {
                                                    Log.d("LobbyViewModel", "➕ Adding new player: $playerName")
                                                    currentPlayers + playerName
                                                } else {
                                                    currentPlayers
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Log.w("LobbyViewModel", "⚠️ Unknown message format: $payload")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("LobbyViewModel", "❌ Error processing message", e)
                                Log.e("LobbyViewModel", "Payload was: $payload")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LobbyViewModel", "❌ Error in lobby subscription", e)
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

    /**
     * Startet das Spiel durch Senden einer Nachricht an das Backend
     */
    fun startGame() {
        viewModelScope.launch {
            try {
                val lobbyId = currentLobbyId ?: run {
                    Log.e("LobbyViewModel", "❌ Kann Spiel nicht starten: Keine Lobby-ID gesetzt")
                    return@launch
                }

                val numericLobbyId = lobbyId.toIntOrNull() ?: run {
                    Log.e(
                        "LobbyViewModel",
                        "❌ Kann Spiel nicht starten: Lobby-ID '$lobbyId' ist keine gültige Zahl"
                    )
                    return@launch
                }

                Log.d("LobbyViewModel", "🎮 Sende Spielstart-Anfrage an Server für Lobby $lobbyId")
                session.sendText("/app/game/start/$numericLobbyId", "")
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

    /**
     * Erzwingt das Setzen des Game-Started-Flags, falls die Server-Kommunikation nicht funktioniert.
     * Diese Funktion dient als Fallback, wenn die Server-Benachrichtigung ausbleibt.
     */
    fun forceTriggerGameStart() {
        if (!_isGameStarted.value) {
            Log.d("LobbyViewModel", "⚠️ Erzwinge Spielstart (Fallback-Mechanismus)")
            _isGameStarted.value = true
        }
    }
}



