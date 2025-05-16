package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.ui.board.BoardFigureManager
import at.aau.serg.sdlapp.ui.board.BoardMoveManager
import at.aau.serg.sdlapp.ui.board.BoardNetworkManager
import at.aau.serg.sdlapp.ui.board.BoardUIManager
import com.otaliastudios.zoom.ZoomLayout
import androidx.compose.ui.platform.ComposeView

class BoardActivity : ComponentActivity(),
    BoardNetworkManager.NetworkCallbacks,
    BoardUIManager.UICallbacks,
    BoardMoveManager.MoveCallbacks {

    private var playerId = 1
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private lateinit var statsButton: ImageButton
    private lateinit var statsOverlayCompose: ComposeView
    private val showStatsOverlay = mutableStateOf(false)

    private lateinit var playerName: String

    private lateinit var playerManager: PlayerManager
    private lateinit var networkManager: BoardNetworkManager
    private lateinit var figureManager: BoardFigureManager
    private lateinit var uiManager: BoardUIManager
    private lateinit var moveManager: BoardMoveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        initializeUIComponents()

        statsOverlayCompose.setContent {
            if (showStatsOverlay.value) {
                PlayerStatsOverlayScreen(
                    playerId = playerId.toString(),
                    onDismiss = {
                        showStatsOverlay.value = false
                    }
                )
            }
        }

        enableFullscreen()
        initializeManagers()
        setupButtonListeners()
        uiManager.showStartChoiceDialog(playerName, networkManager.getStompClient())
        updateStatusText()
    }

    private fun initializeUIComponents() {
        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        diceButton = findViewById(R.id.diceButton)
        statsButton = findViewById(R.id.statsButton)
        statsOverlayCompose = findViewById(R.id.playerStatsOverlayCompose)

        playerName = intent.getStringExtra("playerName") ?: "1"
        playerId = playerName.toIntOrNull() ?: 1
    }

    private fun initializeManagers() {
        playerManager = PlayerManager()
        playerManager.setLocalPlayer(playerId)

        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        figureManager = BoardFigureManager(this, playerManager, boardContainer, boardImage, zoomLayout)
        networkManager = BoardNetworkManager(this, playerManager, playerName, playerId, this)
        uiManager = BoardUIManager(this, playerManager, layoutInflater, this)
        moveManager = BoardMoveManager(this, playerManager, figureManager, this)

        networkManager.connect()
    }

    private fun setupButtonListeners() {
        diceButton.setOnClickListener {
            val diceRoll = (1..6).random()
            println("🎲 Gewürfelt: $diceRoll")
            networkManager.sendRealMove(diceRoll, moveManager.getCurrentFieldIndex())
        }

        statsButton.setOnClickListener {
            showStatsOverlay.value = !showStatsOverlay.value
        }
    }

    override fun onPlayerListReceived(playerIds: List<Int>) {
        val playerIdsToProcess = playerIds.toMutableList()
        if (!playerIdsToProcess.contains(playerId)) playerIdsToProcess.add(playerId)

        playerIdsToProcess.forEach { remotePlayerId ->
            if (!playerManager.playerExists(remotePlayerId)) {
                val player = playerManager.addPlayer(remotePlayerId, "Spieler $remotePlayerId")
                println("➕ Spieler hinzugefügt: ID=$remotePlayerId, Farbe=${player.color}")
            }
        }

        val removedPlayers = playerManager.syncWithActivePlayersList(playerIdsToProcess)
        removedPlayers.forEach { figureManager.removePlayerFigure(it) }
        if (removedPlayers.isNotEmpty()) uiManager.showRemovedPlayersNotification(removedPlayers)

        playerIdsToProcess.forEach { remotePlayerId ->
            if (remotePlayerId != playerId) {
                val remotePlayer = playerManager.getPlayer(remotePlayerId)
                if (remotePlayer != null) {
                    figureManager.getOrCreatePlayerFigure(remotePlayerId)
                    val fieldIndex = remotePlayer.currentFieldIndex
                    if (fieldIndex > 0) {
                        moveManager.updatePlayerPosition(remotePlayerId, fieldIndex)
                    }
                }
            }
        }

        updateStatusText()

        val allPlayers = playerManager.getAllPlayers()
        val hasChanges = removedPlayers.isNotEmpty() || playerIdsToProcess.any { !playerManager.playerExists(it) }
        uiManager.showOtherPlayersNotification(allPlayers, hasChanges)
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        diceButton.isEnabled = isConnected
        diceButton.alpha = if (isConnected) 1.0f else 0.5f

        if (isConnected) {
            Handler(Looper.getMainLooper()).postDelayed({
                uiManager.showActivePlayersInfo()
            }, 3000)
        }
    }

    override fun onConnectionError(errorMessage: String) {
        uiManager.showErrorDialog("Verbindungsfehler", errorMessage)
    }

    override fun onMoveReceived(move: MoveMessage) {
        moveManager.handleMoveMessage(move, playerId, playerName, networkManager.getStompClient())
    }

    override fun onStartFieldSelected(fieldIndex: Int) {
        moveManager.placePlayerAtStartField(playerId, fieldIndex, networkManager.getStompClient(), playerName)
        networkManager.requestActivePlayers()
    }

    override fun onPlayersChanged() {
        updateStatusText()
    }

    private fun enableFullscreen() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun updateStatusText() {
        val statusText = findViewById<TextView>(R.id.statusText)
        uiManager.updateStatusText(statusText)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentFieldIndex", moveManager.getCurrentFieldIndex())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedFieldIndex = savedInstanceState.getInt("currentFieldIndex", 0)
        moveManager.setCurrentFieldIndex(savedFieldIndex)
        moveManager.updatePlayerPosition(playerId, savedFieldIndex)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            boardImage.post {
                moveManager.updatePlayerPosition(playerId, moveManager.getCurrentFieldIndex())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.stopPlayerListUpdateTimer()
    }
}
