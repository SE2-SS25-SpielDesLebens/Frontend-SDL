package at.aau.serg.sdlapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.network.GameClient
import at.aau.serg.sdlapp.network.GameState
import at.aau.serg.sdlapp.network.GameStateListener
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity(), GameStateListener {

    private lateinit var gameClient: GameClient
    private lateinit var gameId: String
    private lateinit var playerName: String
    private lateinit var figure: ImageView
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        // Intent-Daten holen
        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        gameId = intent.getStringExtra("gameId") ?: "game1"

        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        figure = findViewById(R.id.playerImageView)
        diceButton = findViewById(R.id.diceButton)

        // GameClient initialisieren und mit Server verbinden
        gameClient = GameClient(playerName, gameId, this)
        gameClient.connect()

        // Startpfad wählen
        showStartChoiceDialog()

        // 🎲 Button zum Würfeln (1 Schritt)
        diceButton.setOnClickListener {
            gameClient.movePlayer(1) // 1 Schritt würfeln
        }
    }

    private fun showStartChoiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Startweg")
            .setMessage("Willst du direkt ins Berufsleben starten oder studieren?")
            .setPositiveButton("Start normal") { _, _ ->
                gameClient.joinGame(0) // Startfeld 0 (normal)
            }
            .setNegativeButton("Start Uni") { _, _ ->
                gameClient.joinGame(5) // Startfeld 5 (Uni)
            }
            .setCancelable(false)
            .show()
    }

    override fun onGameStateUpdated(gameState: GameState) {
        // Spielfigur auf dem aktuellen Feld platzieren
        val playerField = gameState.getPlayerField(playerName)
        if (playerField != null) {
            moveFigureToField(playerField)
        }
    }

    override fun onChoiceRequired(options: List<Int>) {
        showBranchDialog(options)
    }

    private fun moveFigureToField(field: Field) {
        boardImage.post {
            val x = field.x * boardImage.width
            val y = field.y * boardImage.height

            figure.animate()
                .x(x - figure.width / 2f)
                .y(y - figure.height / 2f)
                .setDuration(1000)
                .start()
        }
    }

    private fun showBranchDialog(options: List<Int>) {
        val labels = options.map { "Gehe zu Feld $it" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Weg")
            .setItems(labels) { _, which ->
                val chosenField = options[which]
                gameClient.chooseField(chosenField)
            }
            .setCancelable(false)
            .show()
    }

    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
