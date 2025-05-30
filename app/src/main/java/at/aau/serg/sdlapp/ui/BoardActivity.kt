package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.ui.board.BoardFigureManager
import at.aau.serg.sdlapp.ui.board.BoardMoveManager
import at.aau.serg.sdlapp.ui.board.BoardNetworkManager
import at.aau.serg.sdlapp.ui.board.BoardUIManager
import com.otaliastudios.zoom.ZoomLayout
import androidx.compose.ui.platform.ComposeView

/**
 * Die BoardActivity ist die Hauptaktivität des Spiels und verwaltet die
 * Spieloberfläche und -logik. Sie ist durch Delegation aufgeteilt in mehrere Manager.
 */
class BoardActivity : ComponentActivity(),
    BoardNetworkManager.NetworkCallbacks,
    BoardUIManager.UICallbacks,
    BoardMoveManager.MoveCallbacks {

    // Kernkomponenten der Activity
    private var playerId = 1
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private lateinit var playerName: String

    // Manager für verschiedene Aspekte des Spiels
    private lateinit var playerManager: PlayerManager
    private lateinit var networkManager: BoardNetworkManager
    private lateinit var figureManager: BoardFigureManager
    private lateinit var uiManager: BoardUIManager
    private lateinit var moveManager: BoardMoveManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        // UI-Komponenten initialisieren
        initializeUIComponents()

        // Manager initialisieren
        initializeManagers()

        // Button-Listener einrichten
        setupButtonListeners()

        // Zeige den Start-Auswahl-Dialog
        uiManager.showStartChoiceDialog(playerName, networkManager.getStompClient())

        // Status-Text initial aktualisieren
        updateStatusText()
    }

    /**
     * Initialisiert die grundlegenden UI-Komponenten
     */    private fun initializeUIComponents() {
        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        diceButton = findViewById(R.id.diceButton)

        // Debug-Meldung zum Board-Status
        boardImage.post {
            Log.d("BoardActivity", "Board geladen: Größe ${boardImage.width}x${boardImage.height}")
            Toast.makeText(this, "Board geladen: ${boardImage.width}x${boardImage.height}", Toast.LENGTH_SHORT).show()
        }

        // Player-ID aus Intent lesen
        playerName = intent.getStringExtra("playerName") ?: "1"
        playerId = playerName.toIntOrNull() ?: 1
        Log.d("BoardActivity", "Spieler initialisiert: ID=$playerId, Name=$playerName")
    }

    /**
     * Initialisiert alle Manager-Komponenten
     */
    private fun initializeManagers() {
        // Player Manager initialisieren
        playerManager = PlayerManager()
        playerManager.setLocalPlayer(playerId)

        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        // BoardFigureManager initialisieren
        figureManager = BoardFigureManager(
            context = this,
            playerManager = playerManager,
            boardContainer = boardContainer,
            boardImage = boardImage,
            zoomLayout = zoomLayout
        )

        // BoardNetworkManager initialisieren
        networkManager = BoardNetworkManager(
            context = this,
            playerManager = playerManager,
            playerName = playerName,
            playerId = playerId,
            callbacks = this
        )
        
        // Fordere die Board-Daten vom Server an
        networkManager.requestBoardData()

        // BoardUIManager initialisieren
        uiManager = BoardUIManager(
            context = this,
            playerManager = playerManager,
            layoutInflater = layoutInflater,
            uiCallbacks = this
        )
        
        // BoardMoveManager initialisieren
        moveManager = BoardMoveManager(
            context = this,
            playerManager = playerManager,
            boardFigureManager = figureManager,
            callbacks = this
        )
        
        // Füge einen Reload-Button hinzu, wenn er benötigt wird
        setupDebugReloadButton()
    }
    
    /**
     * Fügt einen Reload-Button für Debug-Zwecke hinzu
     */
    private fun setupDebugReloadButton() {
        try {
            // Suche nach dem Board-Container
            val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)
            
            // Erstelle einen neuen Button
            val reloadButton = android.widget.Button(this).apply {
                text = "🔄"
                textSize = 18f
                setBackgroundColor(android.graphics.Color.parseColor("#33000000")) // Halbtransparenter Hintergrund
                alpha = 0.7f // Leicht transparent
                
                // Layout-Parameter für den Button (klein in der oberen rechten Ecke)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.TOP or android.view.Gravity.END
                    setMargins(0, 30, 30, 0) // Abstand vom Rand
                }
                
                // Setze Klick-Listener
                setOnClickListener {
                    Toast.makeText(context, "Lade Brett-Daten neu...", Toast.LENGTH_SHORT).show()
                    reloadBoardDataAndPositions()
                }
            }
            
            // Füge den Button zum Layout hinzu
            boardContainer.addView(reloadButton)
            
            Log.d("BoardActivity", "Debug-Reload-Button wurde hinzugefügt")
        } catch (e: Exception) {
            Log.e("BoardActivity", "Fehler beim Hinzufügen des Reload-Buttons: ${e.message}")
        }
    }
    
    /**
     * Richtet die Button-Listener ein
     */    
    private fun setupButtonListeners() {
        // 🎲 Button: würfeln und Bewegung über Backend steuern lassen
        diceButton.setOnClickListener {
            // Zufällige Würfelzahl zwischen 1-10 generieren
            val diceRoll = (1..10).random()

            println("🎲 Gewürfelt: $diceRoll")
            
            // Animiere den Würfelbutton für visuelles Feedback
            diceButton.animate().rotationBy(360f).setDuration(300).start()
            
            // Zeige das Würfelergebnis an
            Toast.makeText(this, "Du hast eine $diceRoll gewürfelt!", Toast.LENGTH_SHORT).show()

            try {
                // Sende die Würfelzahl an das Backend und überlasse ihm die Bewegungsberechnung
                val currentFieldIndex = moveManager.getCurrentFieldIndex()
                networkManager.sendRealMove(diceRoll, currentFieldIndex)
                Log.d("BoardActivity", "Würfelzug $diceRoll gesendet von Feld $currentFieldIndex für Spieler $playerName")
                
                // Zeige Ladezustand an
                Toast.makeText(this, "Berechne Bewegung...", Toast.LENGTH_SHORT).show()
                
                // Die tatsächliche Bewegung erfolgt erst, wenn wir die Antwort vom Server bekommen
                // Dies geschieht über den onMoveReceived Callback
                
                // FALLBACK: Falls keine Antwort kommt, implementiere lokale Bewegung nach Timeout
                Handler(Looper.getMainLooper()).postDelayed({
                    if (moveManager.getCurrentFieldIndex() == currentFieldIndex) {
                        // Wenn die Position sich nicht geändert hat, probiere lokale Bewegung
                        Log.d("BoardActivity", "FALLBACK: Keine Antwort vom Server erhalten, versuche lokale Bewegung")
                        implementLocalMovement(diceRoll, currentFieldIndex)
                    }
                }, 3000) // 3 Sekunden warten
            } catch (e: Exception) {
                Log.e("BoardActivity", "Fehler beim Senden des Würfelzugs: ${e.message}", e)
                Toast.makeText(this, "Fehler beim Senden des Würfelzugs", Toast.LENGTH_SHORT).show()
                
                // Bei einem Fehler versuchen wir direkte lokale Bewegung
                implementLocalMovement(diceRoll, moveManager.getCurrentFieldIndex())
            }
        }

        // 👥 Button: Spielerliste anzeigen
        findViewById<ImageButton>(R.id.playersButton).setOnClickListener {
            // Vor dem Anzeigen nochmal die Spielerliste aktualisieren
            networkManager.requestActivePlayers()

            // Kurz warten, damit die Liste aktualisiert werden kann
            Handler(Looper.getMainLooper()).postDelayed({
                // Spielerliste-Dialog anzeigen
                uiManager.showPlayerListOverlay()
            }, 500) // 500ms warten
        }
    }
    
    /**
     * Implementiert eine lokale Bewegung als Fallback, wenn keine Server-Antwort kommt
     */
    private fun implementLocalMovement(diceRoll: Int, currentFieldIndex: Int) {
        try {
            Log.d("BoardActivity", "Implementiere lokale Bewegung: Würfel $diceRoll von Feld $currentFieldIndex")
            Toast.makeText(this, "Verwende lokale Bewegung (Würfel $diceRoll)", Toast.LENGTH_SHORT).show()
            
            // Berechne neues Feld basierend auf aktuellem Feld + Würfelzahl
            // Dies ist eine vereinfachte Implementierung, die nicht alle Spielregeln berücksichtigt
            val currentField = at.aau.serg.sdlapp.model.board.BoardData.board.find { it.index == currentFieldIndex }
            if (currentField != null) {
                // Finde alle verfügbaren Felder
                val allFields = at.aau.serg.sdlapp.model.board.BoardData.board
                
                // Berechnen des Zielindex (einfache Addition als Fallback)
                var targetIndex = currentFieldIndex + diceRoll
                
                // Stelle sicher, dass wir nicht über das letzte Feld hinausgehen
                val maxIndex = allFields.maxByOrNull { it.index }?.index ?: 20
                if (targetIndex > maxIndex) {
                    targetIndex = maxIndex
                }
                
                // Finde das Zielfeld
                val targetField = allFields.find { it.index == targetIndex }
                            ?: allFields.minByOrNull { Math.abs(it.index - targetIndex) }
                
                if (targetField != null) {
                    Log.d("BoardActivity", "Lokale Bewegung zu Feld ${targetField.index}")
                    
                    // Bewege die Spielfigur
                    val player = playerManager.getLocalPlayer()
                    if (player != null) {
                        figureManager.moveFigureToPosition(targetField.x, targetField.y, player.id)
                        moveManager.setCurrentFieldIndex(targetField.index)
                        playerManager.updatePlayerPosition(player.id, targetField.index)
                        Toast.makeText(this, "Figur zu Feld ${targetField.index} bewegt", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("BoardActivity", "Lokaler Spieler ist null")
                    }
                } else {
                    Log.e("BoardActivity", "Konnte kein passendes Zielfeld finden")
                }
            } else {
                Log.e("BoardActivity", "Aktuelles Feld nicht gefunden: $currentFieldIndex")
                
                // Fallback zum ersten Feld, wenn aktuelles Feld nicht gefunden wird
                val firstField = at.aau.serg.sdlapp.model.board.BoardData.board.firstOrNull()
                if (firstField != null && playerManager.getLocalPlayer() != null) {
                    val player = playerManager.getLocalPlayer()!!
                    figureManager.moveFigureToPosition(firstField.x, firstField.y, player.id)
                    moveManager.setCurrentFieldIndex(firstField.index)
                    playerManager.updatePlayerPosition(player.id, firstField.index)
                    Toast.makeText(this, "Figur zum Startfeld bewegt", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("BoardActivity", "Fehler bei lokaler Bewegung: ${e.message}", e)
            Toast.makeText(this, "Fehler bei lokaler Bewegung: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPlayerListReceived(playerIds: List<Int>) {
        // Füge alle neuen Spieler hinzu und verarbeite entfernte Spieler
        val playerIdsToProcess = playerIds.toMutableList()

        // Stelle sicher, dass der lokale Spieler immer in der Liste ist
        if (!playerIdsToProcess.contains(playerId)) {
            playerIdsToProcess.add(playerId)
        }

        // Füge neue Spieler hinzu
        playerIdsToProcess.forEach { remotePlayerId ->
            if (!playerManager.playerExists(remotePlayerId)) {
                val player = playerManager.addPlayer(remotePlayerId, "Spieler $remotePlayerId")
                println("➕ Spieler hinzugefügt: ID=$remotePlayerId, Farbe=${player.color}")
            }
        }

        // Synchronisiere mit der aktiven Spielerliste und finde entfernte Spieler
        val removedPlayers = playerManager.syncWithActivePlayersList(playerIdsToProcess)

        // Debug-Ausgabe für entfernte Spieler
        if (removedPlayers.isNotEmpty()) {
            println("👋 Entfernte Spieler: $removedPlayers")

            // Entferne die Figuren der nicht mehr aktiven Spieler
            for (removedPlayerId in removedPlayers) {
                figureManager.removePlayerFigure(removedPlayerId)
            }

            // Zeige eine Benachrichtigung wenn Spieler das Spiel verlassen haben
            uiManager.showRemovedPlayersNotification(removedPlayers)
        }

        // Zeige alle Spieler auf dem Brett an
        for (remotePlayerId in playerIdsToProcess) {
            // Wenn es nicht der lokale Spieler ist, zeigen wir ihn an
            if (remotePlayerId != playerId) {
                val remotePlayer = playerManager.getPlayer(remotePlayerId)
                if (remotePlayer != null) {
                    // Holt oder erstellt die Spielfigur (erscheint zuerst bei 0,0)
                    figureManager.getOrCreatePlayerFigure(remotePlayerId)

                    // Wenn der Spieler schon eine Position hat, bewegen wir ihn dorthin
                    val fieldIndex = remotePlayer.currentFieldIndex
                    if (fieldIndex > 0) {
                        moveManager.updatePlayerPosition(remotePlayerId, fieldIndex)
                    }
                }
            }
        }

        // Debug-Ausgabe
        println(playerManager.getDebugSummary())

        // Status-Text aktualisieren
        updateStatusText()

        // Zeigt eine kleine Benachrichtigung über die anderen Spieler, aber nur bei Änderungen
        val allPlayers = playerManager.getAllPlayers()
        val hasChanges = removedPlayers.isNotEmpty() || playerIdsToProcess.any { !playerManager.playerExists(it) }
        uiManager.showOtherPlayersNotification(allPlayers, hasChanges)
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        // Aktiviere/Deaktiviere UI-Elemente je nach Verbindungsstatus
        diceButton.isEnabled = isConnected

        // Zeige visuelles Feedback für Verbindungsstatus
        if (isConnected) {
            diceButton.alpha = 1.0f

            // Zeige nach kurzer Verzögerung die Spielerinformationen an
            Handler(Looper.getMainLooper()).postDelayed({
                uiManager.showActivePlayersInfo()
            }, 3000) // 3 Sekunden warten, damit die Spielerlisten-Anfragen verarbeitet werden können
        } else {
            diceButton.alpha = 0.5f
        }
    }

    override fun onConnectionError(errorMessage: String) {
        // Zeige einen Fehlerdialog an
        uiManager.showErrorDialog("Verbindungsfehler", errorMessage)
    }    override fun onMoveReceived(move: MoveMessage) {
        Log.d("BoardActivity", "🎲🎲🎲 onMoveReceived: $move")
        
        try {
            // Detaillierte Debug-Ausgabe
            Log.d("BoardActivity", "Bewegung für Spieler ${move.playerName} (ID=${move.playerId}) zu Feld ${move.fieldIndex}")
            Log.d("BoardActivity", "Feldtyp: ${move.typeString}, Nächste Felder: ${move.nextPossibleFields}")
            
            // Toast mit Info anzeigen
            Toast.makeText(this, 
                "Bewegung für Spieler ${move.playerName} zu Feld ${move.fieldIndex}", 
                Toast.LENGTH_SHORT
            ).show()
            
            // Verfügbarkeit des Zielfelds prüfen
            val fieldExists = at.aau.serg.sdlapp.model.board.BoardDataManager.fieldExists(move.fieldIndex)
            if (!fieldExists) {
                Log.w("BoardActivity", "⚠️ Zielfeld ${move.fieldIndex} existiert nicht in lokalen BoardData")
                
                // Toast mit Warnung anzeigen
                Toast.makeText(this, 
                    "Warnung: Feld ${move.fieldIndex} nicht lokal vorhanden", 
                    Toast.LENGTH_LONG
                ).show()
            }
            
            // Bewegungsnachricht an Move-Manager übergeben
            moveManager.handleMoveMessage(move, playerId, playerName, networkManager.getStompClient())
            
        } catch (e: Exception) {
            // Bei einem Fehler ausführliche Log-Ausgabe und Toast
            Log.e("BoardActivity", "❌ Fehler bei der Verarbeitung einer Bewegungsnachricht: ${e.message}", e)
            Toast.makeText(this, 
                "Fehler bei der Verarbeitung der Bewegung: ${e.message}", 
                Toast.LENGTH_LONG
            ).show()
            
            // Versuche, durch Neuladung der Brett-Daten zu beheben
            Handler(Looper.getMainLooper()).postDelayed({
                reloadBoardDataAndPositions()
            }, 2000) // 2 Sekunden Verzögerung
        }
    }override fun onStartFieldSelected(fieldIndex: Int) {
        moveManager.placePlayerAtStartField(playerId, fieldIndex, networkManager.getStompClient(), playerName)
    }

    override fun onPlayersChanged() {
        // Status-Text aktualisieren da sich die Spielerliste geändert hat
        updateStatusText()
    }

    override fun onBoardDataReceived(fields: List<Field>) {
        // Log that we received board data
        Log.d("BoardActivity", "Received board data from server: ${fields.size} fields")
        
        // Utilize the BoardDataManager to synchronize the data
        if (fields.isNotEmpty()) {
            val synchronized = at.aau.serg.sdlapp.model.board.BoardDataManager.synchronizeBoardData(fields)
            
            if (synchronized) {
                Log.d("BoardActivity", "Board data successfully synchronized with server data")
                
                // Log detailed field information for debugging purposes
                at.aau.serg.sdlapp.model.board.BoardDataManager.logBoardData()
                
                // Run a direct comparison to spot any critical differences
                val frontendFields = at.aau.serg.sdlapp.model.board.BoardData.board
                val serverFieldsById = fields.associateBy { it.index }
                
                for (frontendField in frontendFields) {
                    val serverField = serverFieldsById[frontendField.index]
                    
                    if (serverField != null) {
                        // Check for significant coordinate differences
                        val xDiff = Math.abs(frontendField.x - serverField.x)
                        val yDiff = Math.abs(frontendField.y - serverField.y)
                        
                        if (xDiff > 0.1 || yDiff > 0.1) {
                            Log.w("BoardActivity", "Field ${frontendField.index} has significant position difference: " +
                                   "Frontend (${frontendField.x}, ${frontendField.y}) vs " +
                                   "Server (${serverField.x}, ${serverField.y})")
                        }
                    } else {
                        Log.w("BoardActivity", "Field ${frontendField.index} exists in frontend but not on server")
                    }
                }
            } else {
                Log.e("BoardActivity", "Failed to synchronize board data from server")
            }
        } else {
            Log.w("BoardActivity", "Received empty board data from server")
        }
    }

    /**
     * Aktiviert den Vollbildmodus
     */
    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Aktualisiert den Status-Text mit der Anzahl der aktiven Spieler
     */
    private fun updateStatusText() {
        val statusText = findViewById<TextView>(R.id.statusText)
        uiManager.updateStatusText(statusText)
    }

    /**
     * Speichert den Zustand der Activity, falls sie neu erstellt werden muss.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Speichere den aktuellen Feld-Index
        outState.putInt("currentFieldIndex", moveManager.getCurrentFieldIndex())
        println("💾 Activity-Zustand gespeichert, currentFieldIndex=${moveManager.getCurrentFieldIndex()}")
    }

    /**
     * Stellt den Zustand der Activity wieder her.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Stelle den Feld-Index wieder her
        val savedFieldIndex = savedInstanceState.getInt("currentFieldIndex", 0)
        moveManager.setCurrentFieldIndex(savedFieldIndex)
        println("📂 Activity-Zustand wiederhergestellt, currentFieldIndex=$savedFieldIndex")

        // Position aktualisieren
        moveManager.updatePlayerPosition(playerId, savedFieldIndex)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Stelle sicher, dass alle UI-Elemente initialisiert sind
            boardImage.post {
                println("🚗 BoardActivity: Fenster hat Fokus bekommen")

                // Falls currentFieldIndex bereits gesetzt ist, positioniere die Figur korrekt
                moveManager.updatePlayerPosition(playerId, moveManager.getCurrentFieldIndex())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Timer stoppen, wenn die Activity zerstört wird
        networkManager.stopPlayerListUpdateTimer()
        println("🚪 BoardActivity: onDestroy()")
    }
    
    /**
     * Forciert eine manuelle Neuladung der Board-Daten und
     * Neupositionierung der Spielfiguren.
     */
    private fun reloadBoardDataAndPositions() {
        // Toast als visuelles Feedback zeigen
        Toast.makeText(this, "Brett-Daten werden neu geladen...", Toast.LENGTH_SHORT).show()
        
        // Board-Daten neu vom Server anfordern
        networkManager.requestBoardData()
        
        // Kurze Verzögerung, damit die Daten geladen werden können
        Handler(Looper.getMainLooper()).postDelayed({
            // Log-Ausgabe der aktuellen Board-Dimensionen
            Log.d("BoardActivity", "Board-Dimensionen: ${boardImage.width}x${boardImage.height}")
            
            // Alle aktiven Spieler vom Server anfordern
            networkManager.requestActivePlayers()
            
            // Aktualisieren des lokalen Spielers
            val localPlayer = playerManager.getLocalPlayer()
            val currentFieldIndex = moveManager.getCurrentFieldIndex()
            
            if (localPlayer != null && currentFieldIndex > 0) {
                // Position des lokalen Spielers aktualisieren
                Log.d("BoardActivity", "Aktualisiere Position des lokalen Spielers zu Feld $currentFieldIndex")
                moveManager.updatePlayerPosition(localPlayer.id, currentFieldIndex)
                
                // Status-Text aktualisieren
                updateStatusText()
            }
            
            // Toast mit Info anzeigen
            Toast.makeText(this, "Daten geladen, Positionen aktualisiert", Toast.LENGTH_SHORT).show()
        }, 1000) // 1 Sekunde warten
    }
}