package at.aau.serg.sdlapp.model.board

// JUnit 4 Imports (Standard für Android)
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit-Tests für die Board-Klasse.
 * Diese Tests verwenden JUnit4, das Standardtestframework für Android.
 */
@RunWith(JUnit4::class)
class BoardTest {

    private lateinit var board: Board
    private lateinit var testFields: List<Field>
    
    @Before
    fun setUp() {
        // Erstelle eine einfache Testumgebung mit Feldern
        testFields = listOf(
            Field(index = 0, x = 0.1f, y = 0.1f, nextFields = listOf(1), type = FieldType.STARTNORMAL),
            Field(index = 1, x = 0.2f, y = 0.2f, nextFields = listOf(2, 3), type = FieldType.AKTION),
            Field(index = 2, x = 0.3f, y = 0.3f, nextFields = listOf(4), type = FieldType.ZAHLTAG),
            Field(index = 3, x = 0.4f, y = 0.4f, nextFields = listOf(5), type = FieldType.FREUND),
            Field(index = 4, x = 0.5f, y = 0.5f, nextFields = listOf(), type = FieldType.RUHESTAND),
            Field(index = 5, x = 0.6f, y = 0.6f, nextFields = listOf(), type = FieldType.RUHESTAND)
        )
        
        // Instanziiere das Board mit den Testfeldern
        board = Board(testFields)
    }
    
    @Test
    fun testBoardInitialization() {
        // Überprüfe, ob das Board korrekt initialisiert wurde
        assertEquals(6, board.fields.size)
        assertEquals(0, board.fields[0].index)
        assertEquals(FieldType.STARTNORMAL, board.fields[0].type)
    }
    
    @Test
    fun testAddPlayer() {
        // When
        board.addPlayer("player1", 0)
        
        // Then
        val playerField = board.getPlayerField("player1")
        assertEquals(0, playerField.index)
    }
    
    @Test
    fun testMovePlayer() {
        // Given
        board.addPlayer("player1", 0)
        
        // When
        board.movePlayer("player1", 1)
        
        // Then
        val playerField = board.getPlayerField("player1")
        assertEquals(1, playerField.index)
    }
    
    @Test
    fun testMovePlayerStopsAtBranch() {
        // Given
        board.addPlayer("player1", 0)
        
        // When - trying to move 2 steps, but should stop at field 1 because it has a branch
        board.movePlayer("player1", 2)
        
        // Then - player should be at field 1, not field 2
        val playerField = board.getPlayerField("player1")
        assertEquals(1, playerField.index)
    }
    
    @Test
    fun testMovePlayerStopsAtDeadEnd() {
        // Given
        board.addPlayer("player1", 2) // Start at field 2, which connects to field 4 (dead end)
        
        // When - try to move 2 steps
        board.movePlayer("player1", 2)
        
        // Then - player should have stopped at field 4 (dead end)
        val playerField = board.getPlayerField("player1")
        assertEquals(4, playerField.index)
    }
    
    @Test
    fun testManualMoveTo() {
        // Given
        board.addPlayer("player1", 1) // Field 1 has next fields 2 and 3
        
        // When
        board.manualMoveTo("player1", 3) // Manually choose path to field 3
        
        // Then
        val playerField = board.getPlayerField("player1")
        assertEquals(3, playerField.index)
    }
    
    @Test
    fun testManualMoveToInvalidField() {
        // Given
        board.addPlayer("player1", 0) // Field 0 only has next field 1
        
        // When - try to move to field 2 which is not connected to field 0
        board.manualMoveTo("player1", 2)
        
        // Then - player should remain at field 0
        val playerField = board.getPlayerField("player1")
        assertEquals(0, playerField.index)
    }
    
    @Test
    fun testGetPlayerFieldForNonExistentPlayer() {
        // When - get field for a player that doesn't exist
        val field = board.getPlayerField("notexistingplayer")
        
        // Then - should return field 0 (default)
        assertEquals(0, field.index)
    }
    
    @Test
    fun testMultiplePlayers() {
        // Given
        board.addPlayer("player1", 0)
        board.addPlayer("player2", 1)
        
        // When
        board.movePlayer("player1", 1)
        
        // Then - player 1 should have moved, player 2 should be unchanged
        assertEquals(1, board.getPlayerField("player1").index)
        assertEquals(1, board.getPlayerField("player2").index)
    }
}