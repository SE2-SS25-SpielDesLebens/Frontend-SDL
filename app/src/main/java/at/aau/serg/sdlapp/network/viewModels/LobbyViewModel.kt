package at.aau.serg.sdlapp.network.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.sdlapp.model.player.PlayerModell
import at.aau.serg.sdlapp.model.player.PlayerRepository
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

        // 🆕 Spieler sofort am Server anlegen
        viewModelScope.launch {
            try {
                Log.d("LobbyViewModel", "🟢 Registriere Spieler $currentPlayer am Server...")
                val newPlayer = PlayerModell(
                    id = currentPlayer,
                    children = 0,
                    education = false,
                    investments = 0,
                    money = 250_000,
                    salary = 50_000
                )

                PlayerRepository.createPlayer(newPlayer)
                Log.d("LobbyViewModel", "✅ Spieler $currentPlayer erfolgreich registriert")
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "❌ Spieler konnte nicht erstellt werden: ${e.message}")
            }
        }

        // 🛰️ Game-Status vom Server beobachten
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
        updatesJob = viewModelScope.launch {
            try {
                session.subscribeText("/topic/$lobbyId").collect { payload ->
                    val json = JSONObject(payload)
                    // Prüfe auf vollständige Spielerliste (LobbyUpdateMessage)
                    if (json.has("player1")) {
                        val players = listOfNotNull(
                            json.optString("player1").takeIf { !it.isNullOrBlank() },
                            json.optString("player2").takeIf { !it.isNullOrBlank() },
                            json.optString("player3").takeIf { !it.isNullOrBlank() },
                            json.optString("player4").takeIf { !it.isNullOrBlank() }
                        )
                        _players.value = players
                        // Prüfe, ob das Spiel gestartet ist
                        if (json.has("isStarted")) {
                            val isStarted = json.getBoolean("isStarted")
                            Log.d(
                                "LobbyViewModel",
                                "🔍 isStarted vom Server empfangen: $isStarted (aktuell: ${_isGameStarted.value})"
                            )
                            if (isStarted && !_isGameStarted.value) {
                                Log.d(
                                    "LobbyViewModel",
                                    "🎮 Spiel wurde vom Server gestartet! Setze isGameStarted = true"
                                )
                                _isGameStarted.value = true
                            }
                        } else {
                            Log.d(
                                "LobbyViewModel",
                                "⚠️ isStarted-Feld fehlt in der Server-Antwort: $payload"
                            )
                        }
                    }
                    // Prüfe auf einzelne Join-Response (LobbyResponseMessage)
                    else if (json.has("playerName")) {
                        val playerName = json.getString("playerName")
                        _players.update { currentPlayers ->
                            if (!currentPlayers.contains(playerName)) {
                                currentPlayers + playerName
                            } else {
                                currentPlayers
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Error in updates flow", e)
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



