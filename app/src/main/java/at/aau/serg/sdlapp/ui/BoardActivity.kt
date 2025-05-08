package at.aau.serg.sdlapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.Board
import at.aau.serg.sdlapp.model.board.BoardData
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity() {

    private lateinit var board: Board
    private var playerId = 1
    private lateinit var figure: ImageView
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private lateinit var statsLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        board = Board(BoardData.board)

        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        figure = findViewById(R.id.playerImageView)
        diceButton = findViewById(R.id.diceButton)

        statsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                println("Spielerwerte wurden möglicherweise geändert – Backend neu abfragen")
            }
        }


        // Startpfad wählen
        showStartChoiceDialog()

        // 🎲 Button zum Würfeln (1 Schritt)
        diceButton.setOnClickListener {
            moveOneStep()
        }

        val btnShowStats = findViewById<ImageButton>(R.id.btnShowStats)
        btnShowStats.setOnClickListener {
            val intent = Intent(this, PlayerStatsActivity::class.java)
            intent.putExtra("playerId", playerId)
            statsLauncher.launch(intent) //über Launcher starten
        }

    }

    private fun showStartChoiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Startweg")
            .setMessage("Willst du direkt ins Berufsleben starten oder studieren?")
            .setPositiveButton("Start normal") { _, _ ->
                board.addPlayer(playerId, 0)
                moveFigureToField(playerId)
            }
            .setNegativeButton("Start Uni") { _, _ ->
                board.addPlayer(playerId, 5)
                moveFigureToField(playerId)
            }
            .setCancelable(false)
            .show()
    }

    private fun moveOneStep() {
        val currentField = board.getPlayerField(playerId)

        if (currentField.nextFields.isEmpty()) return

        if (currentField.nextFields.size > 1) {
            showBranchDialog(playerId, currentField.nextFields)
        } else {
            board.manualMoveTo(playerId, currentField.nextFields.first())
            moveFigureToField(playerId)
        }
    }

    private fun moveFigureToField(playerId: Int) {
        val field = board.getPlayerField(playerId)

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

    private fun showBranchDialog(playerId: Int, options: List<Int>) {
        val labels = options.map { "Gehe zu Feld $it" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Weg")
            .setItems(labels) { _, which ->
                val chosenField = options[which]
                board.manualMoveTo(playerId, chosenField)
                moveFigureToField(playerId)
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


}
