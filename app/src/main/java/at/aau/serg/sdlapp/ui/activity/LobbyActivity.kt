package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import at.aau.serg.sdlapp.network.viewModels.LobbyViewModel
import at.aau.serg.sdlapp.network.viewModels.LobbyViewModelFactory
import at.aau.serg.sdlapp.network.viewModels.getSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession


class LobbyActivity : ComponentActivity() {
    private lateinit var lobbyID: String
    private lateinit var playerName: String
    private lateinit var lobbyViewModel: LobbyViewModel
    private lateinit var session: StompSession
    private val viewModel by lazy { getSharedViewModel() }
    private val scope = CoroutineScope(Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = viewModel.myStomp.value?.sessionOrNull ?: run {
            Log.d("Debugging", "session is null")
            finish()
            return
        }

        this.lobbyViewModel = ViewModelProvider(
            this,
            LobbyViewModelFactory(session)
        )[LobbyViewModel::class.java]

        lobbyID = intent.getStringExtra("lobbyID") ?: run {
            finish()
            return
        }
        playerName = intent.getStringExtra("player") ?: run {
            finish()
            return
        }

        // GameStart-Listener für direkte Benachrichtigungen vom Server einrichten
        viewModel.myStomp.value?.let { stompManager ->
            stompManager.subscribeToGameStatus(lobbyID) {
                Log.d("LobbyActivity", "🎮 Game status subscription: Game started notification received!")
                lobbyViewModel.forceTriggerGameStart()
            }
        }

        // Observe isGameStarted to handle navigation outside of Composable
        scope.launch {
            lobbyViewModel.isGameStarted.collect { isStarted ->
                if (isStarted) {
                    Log.d("LobbyActivity", "🎮 Flow observer: Game started state changed to true")
                    navigateToBoardActivity()
                }
            }
        }

        lobbyViewModel.initialize(lobbyID, playerName)

        setContent {
            LobbyScreen(viewModel = lobbyViewModel)
        }
    }
    
    /**
     * Helper method to navigate to the BoardActivity
     */
    private fun navigateToBoardActivity() {
        Log.d("LobbyActivity", "🎮 Navigating to BoardActivity...")
        val intent = Intent(this@LobbyActivity, BoardActivity::class.java).apply {
            putExtra("playerName", playerName)
            putExtra("lobbyID", lobbyID)
        }
        startActivity(intent)
        finish() // Close LobbyActivity
    }    @Composable
    fun LobbyScreen(viewModel: LobbyViewModel) {
        val textColor = Color.White
        val players by viewModel.players.collectAsState()
        val isGameStarted by viewModel.isGameStarted.collectAsState()
        
        // Logging für Debug-Zwecke
        Log.d("LobbyActivity", "🔄 LobbyScreen recomposing: isGameStarted=$isGameStarted, players=${players.size}")
        
        // Note: Die Navigation wird jetzt über den Flow-Observer in onCreate() gehandelt,
        // nicht mehr direkt in der Composable-Funktion

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lobby: $lobbyID",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(top = 20.dp)
            )
            Text(
                text = "Spieler:",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .padding(10.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(count = players.size) { index ->
                    Text(text = players[index],
                        color = textColor,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center)
                }
            }
            val isStartingGame = remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    if (isStartingGame.value) return@Button // Verhindere Mehrfachklicks
                    
                    // UI-Status aktualisieren
                    isStartingGame.value = true
                    
                    // Spiel auf dem Backend starten
                    Log.d("LobbyActivity", "Spielstart-Button gedrückt, starte Spiel auf dem Server...")
                    viewModel.startGame()
                    
                    // Zusätzliche Sicherheit: Falls das isGameStarted-Flag nicht korrekt gesetzt wird,
                    // führen wir nach einer kurzen Verzögerung trotzdem die Navigation aus
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!viewModel.isGameStarted.value) {
                            Log.d("LobbyActivity", "⏱️ Timeout - Keine Bestätigung vom Server erhalten, leite trotzdem weiter...")
                            viewModel.forceTriggerGameStart()
                        }
                        // Reset status falls nötig
                        isStartingGame.value = false
                    }, 3000) // 3 Sekunden Timeout
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally),
                enabled = !isStartingGame.value
            ) {
                Text(if (isStartingGame.value) "Spiel wird gestartet..." else "Spiel starten")
            }
            Button(
                onClick = {
                    leaveLobby()
                    finish()
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 30.dp)
            ) {
                Text("Lobby verlassen")
            }
        }
    }

    fun leaveLobby(){
        scope.launch {
            viewModel.myStomp.value?.sendLobbyLeave(playerName, lobbyID)
        }
    }
}