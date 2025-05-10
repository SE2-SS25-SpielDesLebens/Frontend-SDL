package at.aau.serg.sdlapp.ui

import android.widget.ImageButton
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.board.FieldTyp
// Aktualisierte Importe
import at.aau.serg.sdlapp.network.MoveResult
import at.aau.serg.sdlapp.network.Player
import at.aau.serg.sdlapp.network.GameClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(MockitoJUnitRunner::class)
class BoardActivityTest {

    private lateinit var activityController: ActivityController<BoardActivity>
    private lateinit var activity: BoardActivity
    
    @Mock
    private lateinit var mockGameClient: GameClient
    
    @Before
    fun setUp() {
        // Mock Intent-Extra-Daten
        val intent = BoardActivity.createIntent(
            mockContext = mock(),
            playerName = "TestPlayer",
            gameId = "game1"
        )
        
        // Activity mit Mock-Daten erstellen
        activityController = Robolectric.buildActivity(BoardActivity::class.java, intent)
        activity = activityController.get()
        
        // GameClient mit Mock austauschen
        activity.gameClient = mockGameClient
    }
    
    @Test
    fun testOnCreate_connectsToServer() {
        // When
        activityController.create().start().resume()
        
        // Then
        verify(mockGameClient).connect()
    }
    
    @Test
    fun testDiceButtonClick_callsMovePlayer() {
        // Given
        activityController.create().start().resume()
        val diceButton = activity.findViewById<ImageButton>(at.aau.serg.sdlapp.R.id.diceButton)
        
        // When
        diceButton.performClick()
        
        // Then
        verify(mockGameClient).movePlayer(1)
    }
    
    @Test
    fun testOnGameStateUpdated_updatesFigurePosition() {
        // Given
        activityController.create().start().resume()
        val gameState = createMockGameState()
        
        // When
        activity.onGameStateUpdated(gameState)
        
        // Überprüfe visuell mit dem Debugger oder verwende Espresso für UI-Tests
    }
    
    @Test
    fun testOnChoiceRequired_showsDialog() {
        // Given
        activityController.create().start().resume()
        val options = listOf(5, 10)
        
        // When
        activity.onChoiceRequired(options)
        
        // Überprüfe mit Espresso, dass der Dialog angezeigt wird
    }
    
    // Hilfsmethode zum Erstellen eines Mock-GameState
    private fun createMockGameState(): GameState {
        val testField = Field(1, 0.2f, 0.2f, listOf(2), FieldTyp.ZAHLTAG)
        
        return GameState(
            gameId = "game1",
            players = listOf(Player(name = "TestPlayer", fieldIndex = 1)),
            currentPlayer = "TestPlayer",
            fields = mapOf("1" to testField)
        )
    }
}