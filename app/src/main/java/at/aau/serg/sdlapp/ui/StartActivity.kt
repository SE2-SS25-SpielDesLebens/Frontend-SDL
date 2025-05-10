package at.aau.serg.sdlapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.GameClient
import at.aau.serg.sdlapp.network.GameState
import at.aau.serg.sdlapp.network.GameStateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class StartActivity : ComponentActivity(), GameStateListener {
    
    private lateinit var statusText: TextView
    private var gameClient: GameClient? = null
    private var timeoutJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        enableFullscreen()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val startBtn = findViewById<Button>(R.id.startGameBtn)
        val settingsBtn = findViewById<ImageButton>(R.id.settingsBtn)

        // Status Text für WebSocket-Tests
        statusText = TextView(this)
        statusText.text = "WebSocket-Status: Nicht verbunden"
        // Füge das TextView zum Layout hinzu (direkt unter dem Namen-Input)
        val parent = nameInput.parent as android.view.ViewGroup
        parent.addView(statusText, parent.indexOfChild(nameInput) + 1)
        
        // Spiel starten → NEU: Öffnet BoardActivity
        startBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                // WebSocket-Verbindung testen vor dem Spielstart
                testWebSocketConnection(name)
            } else {
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show()
            }
        }

        // Einstellungen öffnen
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun testWebSocketConnection(playerName: String) {
        // Zurücksetzen für neuen Verbindungsversuch
        cancelTimeoutJob()
        
        statusText.text = "WebSocket-Status: Verbindung wird hergestellt..."
        Log.d("WebSocketTest", "Starte Verbindungsversuch mit Server")
        
        // Zeige Fortschritt im Status-Text an
        timeoutJob = scope.launch {
            for (i in 1..6) {
                delay(1000)
                if (i == 6) {
                    // Nach 6 Sekunden ohne Rückmeldung vom Server
                    statusText.text = "WebSocket-Status: Timeout! Server nicht erreichbar"
                    Toast.makeText(
                        this@StartActivity, 
                        "Server antwortet nicht. Läuft der Backend-Server?", 
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                // Punkte zur Animation hinzufügen
                val dots = ".".repeat(i % 4)
                statusText.text = "WebSocket-Status: Verbindung wird hergestellt$dots"
            }
        }
        
        // GameClient instanziieren und Verbindung aufbauen
        gameClient = GameClient(playerName, "test-game-${System.currentTimeMillis()}", this)
        gameClient?.connect()
        
        // Nach 3 Sekunden versuchen, ein Spiel zu erstellen (nur wenn die Verbindung erfolgreich war)
        // Das müssen wir nicht mehr hier machen, sondern erst nach dem erfolgreichen Verbindungsaufbau
    }
    
    private fun cancelTimeoutJob() {
        timeoutJob?.cancel()
        timeoutJob = null
    }
    
    // GameStateListener Implementation
    override fun onGameStateUpdated(gameState: GameState) {
        runOnUiThread {
            cancelTimeoutJob()  // Timeout-Job abbrechen, da Verbindung erfolgreich
            
            val msg = "Spielstatus aktualisiert: ${gameState.playerPositions.size} Spieler"
            Log.i("WebSocketTest", msg)
            statusText.text = "WebSocket-Status: Verbunden!"
            
            // Nach erfolgreicher Verbindung SOFORT zur BoardActivity navigieren
            val name = findViewById<EditText>(R.id.nameInput).text.toString()
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("playerName", name)
            startActivity(intent)
        }
    }

    override fun onChoiceRequired(options: List<Int>) {
        // Für diesen Test nicht relevant
    }

    override fun onError(message: String) {
        runOnUiThread {
            cancelTimeoutJob()  // Timeout-Job abbrechen, da wir eine Antwort erhalten haben
            
            Log.e("WebSocketTest", "Fehler: $message")
            statusText.text = "WebSocket-Status: Fehler! $message"
            Toast.makeText(this, "WebSocket-Fehler: $message", Toast.LENGTH_LONG).show()
            
            // Hinzufügen eines Hinweises zum Starten des Servers
            if (message.contains("Verbindungs-Timeout") || message.contains("not")) {
                statusText.text = statusText.text.toString() + "\nBitte stelle sicher, dass der Backend-Server läuft."
                Toast.makeText(this, "Backend-Server läuft nicht. Bitte starten!", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Verbindung sauber beenden, wenn die Activity zerstört wird
        gameClient = null
        // Coroutine-Scope beenden
        scope.cancel()
    }
    
    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
