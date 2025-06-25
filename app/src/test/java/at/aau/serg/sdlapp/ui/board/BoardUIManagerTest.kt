package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import at.aau.serg.sdlapp.model.player.CarColor
import at.aau.serg.sdlapp.model.player.Player
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class BoardUIManagerTest {

    private lateinit var context: Context
    private lateinit var inflater: LayoutInflater
    private lateinit var stompClient: StompConnectionManager
    private lateinit var callbacks: BoardUIManager.UICallbacks
    private lateinit var playerManager: PlayerManager
    private lateinit var boardUIManager: BoardUIManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        inflater = mockk(relaxed = true)
        stompClient = mockk(relaxed = true)
        callbacks = mockk(relaxed = true)
        playerManager = mockk(relaxed = true)

        mockkStatic(Toast::class)

        boardUIManager = BoardUIManager(context, playerManager, inflater, callbacks)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showNewPlayerNotification shows Toast`() {
        val slot = slot<CharSequence>()
        every {
            Toast.makeText(context, capture(slot), Toast.LENGTH_SHORT)
        } returns mockk(relaxed = true)

        boardUIManager.showNewPlayerNotification("X123")

        assert(slot.captured.contains("X123"))
    }

    @Test
    fun `showRemovedPlayersNotification shows correct message for one player`() {
        val slot = slot<CharSequence>()
        every {
            Toast.makeText(context, capture(slot), Toast.LENGTH_SHORT)
        } returns mockk(relaxed = true)

        boardUIManager.showRemovedPlayersNotification(listOf("A"))

        assert(slot.captured.contains("Spieler A hat das Spiel verlassen"))
    }

    @Test
    fun `showRemovedPlayersNotification shows correct message for multiple players`() {
        val slot = slot<CharSequence>()
        every {
            Toast.makeText(context, capture(slot), Toast.LENGTH_SHORT)
        } returns mockk(relaxed = true)

        boardUIManager.showRemovedPlayersNotification(listOf("A", "B"))

        assert(slot.captured.contains("2 Spieler haben das Spiel verlassen"))
    }

    @Test
    fun `updateStatusText sets correct text`() {
        val textView = mockk<TextView>(relaxed = true)
        val players = mapOf(
            "1" to Player("1", "Test1", 0),
            "2" to Player("2", "Test2", 0)
        )
        every { playerManager.getAllPlayers() } returns players

        boardUIManager.updateStatusText(textView)

        verify { textView.text = "2 Spieler online" }
    }
}
