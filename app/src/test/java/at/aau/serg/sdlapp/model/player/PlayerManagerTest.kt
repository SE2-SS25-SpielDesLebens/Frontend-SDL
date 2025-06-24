package at.aau.serg.sdlapp.model.player

import at.aau.serg.sdlapp.model.game.GameConstants
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlayerManagerTest {

    @Before
    fun setUp() {
        PlayerManager.clearPlayers()
    }

    @After
    fun tearDown() {
        PlayerManager.clearPlayers()
    }

    @Test
    fun testAddPlayer() {
        val player = PlayerManager.addPlayer("1", "Alice", 5)
        assertEquals("1", player.id)
        assertEquals("Alice", player.name)
        assertEquals(5, player.currentFieldIndex)
        assertTrue(PlayerManager.playerExists("1"))
    }

    @Test
    fun testSetAndGetLocalPlayer() {
        PlayerManager.addPlayer("2", "Bob")
        PlayerManager.setLocalPlayer("2")
        val local = PlayerManager.getLocalPlayer()
        assertNotNull(local)
        assertEquals("2", local!!.id)
    }

    @Test
    fun testSetLocalPlayerCreatesIfMissing() {
        PlayerManager.setLocalPlayer("3")
        val local = PlayerManager.getLocalPlayer()
        assertNotNull(local)
        assertEquals("3", local!!.id)
        assertEquals("Spieler 3", local.name)
    }

    @Test
    fun testUpdatePlayerPosition() {
        PlayerManager.addPlayer("4", "Charlie")
        PlayerManager.updatePlayerPosition("4", 10)
        val updated = PlayerManager.getPlayer("4")
        assertEquals(10, updated!!.currentFieldIndex)
    }

    @Test
    fun testRemovePlayer() {
        PlayerManager.addPlayer("5", "Dana")
        val removed = PlayerManager.removePlayer("5")
        assertEquals("5", removed!!.id)
        assertFalse(PlayerManager.playerExists("5"))
    }

    @Test
    fun testRemoveLocalPlayerNotAllowed() {
        PlayerManager.setLocalPlayer("6")
        val removed = PlayerManager.removePlayer("6")
        assertNull(removed)
        assertTrue(PlayerManager.playerExists("6"))
    }

    @Test
    fun testSyncWithActivePlayersList() {
        PlayerManager.addPlayer("7", "Eve")
        PlayerManager.addPlayer("8", "Frank")
        PlayerManager.setLocalPlayer("8")
        val removed = PlayerManager.syncWithActivePlayersList(listOf("8"))
        assertTrue(removed.contains("7"))
        assertFalse(PlayerManager.playerExists("7"))
        assertTrue(PlayerManager.playerExists("8"))
    }

    @Test
    fun testGetAllPlayers() {
        PlayerManager.addPlayer("9", "Gina")
        PlayerManager.addPlayer("10", "Hugo")
        val all = PlayerManager.getAllPlayers()
        assertEquals(2, all.size)
    }


    @Test
    fun testHaveAllPlayersFinished_SinglePlayer() {
        PlayerManager.addPlayer("11", "Isa", GameConstants.FINAL_FIELD_INDICES.first())
        assertTrue(PlayerManager.haveAllPlayersFinished())
    }

    @Test
    fun testHaveAllPlayersFinished_MultiplePlayers_AllOnFinal() {
        GameConstants.FINAL_FIELD_INDICES.forEachIndexed { i, idx ->
            PlayerManager.addPlayer((i + 20).toString(), "Player$i", idx)
        }
        assertTrue(PlayerManager.haveAllPlayersFinished())
    }

    @Test
    fun testHaveAllPlayersFinished_MultiplePlayers_NotAllOnFinal() {
        PlayerManager.addPlayer("30", "Jay", GameConstants.FINAL_FIELD_INDICES[0])
        PlayerManager.addPlayer("31", "Kai", 1)
        assertFalse(PlayerManager.haveAllPlayersFinished())
    }


    @Test
    fun testIsLocalPlayer() {
        PlayerManager.setLocalPlayer("32")
        assertTrue(PlayerManager.isLocalPlayer("32"))
        assertFalse(PlayerManager.isLocalPlayer("33"))
    }

    @Test
    fun testPlayerExists() {
        PlayerManager.addPlayer("34", "Luna")
        assertTrue(PlayerManager.playerExists("34"))
    }

    @Test
    fun testDebugSummary() {
        PlayerManager.setLocalPlayer("35")
        val summary = PlayerManager.getDebugSummary()
        assertTrue(summary.contains("*")) // local player markiert
    }

    @Test
    fun testGameFinishedState() {
        assertFalse(PlayerManager.isGameFinished())
        PlayerManager.markGameFinished()
        assertTrue(PlayerManager.isGameFinished())
    }

    @Test
    fun testUpdatePlayerColor() {
        // Given
        val playerId = "40"
        PlayerManager.addPlayer(playerId, "Max")
        
        // When
        PlayerManager.updatePlayerColor(playerId, "RED")
        
        // Then
        val player = PlayerManager.getPlayer(playerId)
        assertEquals(CarColor.RED, player?.color)
    }

    @Test
    fun testGetAllPlayersAsList() {
        // Given
        PlayerManager.addPlayer("41", "Alice")
        PlayerManager.addPlayer("42", "Bob")
        
        // When
        val playerList = PlayerManager.getAllPlayersAsList()
        
        // Then
        assertEquals(2, playerList.size)
        assertTrue(playerList.any { it.id == "41" && it.name == "Alice" })
        assertTrue(playerList.any { it.id == "42" && it.name == "Bob" })
    }

    @Test
    fun testClearPlayers() {
        // Given
        PlayerManager.addPlayer("43", "Alice")
        PlayerManager.addPlayer("44", "Bob")
        assertTrue(PlayerManager.getAllPlayers().isNotEmpty())
        
        // When
        PlayerManager.clearPlayers()
        
        // Then
        assertTrue(PlayerManager.getAllPlayers().isEmpty())
    }

    @Test
    fun testGetAllPlayerIds() {
        // Given
        PlayerManager.addPlayer("45", "Alice")
        PlayerManager.addPlayer("46", "Bob")
        
        // When
        val playerIds = PlayerManager.getAllPlayerIds()
        
        // Then
        assertEquals(2, playerIds.size)
        assertTrue(playerIds.contains("45"))
        assertTrue(playerIds.contains("46"))
    }

    @Test
    fun testUpdatePlayerColorWithInvalidPlayerId() {
        // Given
        val nonExistentPlayerId = "999"
        
        // When
        PlayerManager.updatePlayerColor(nonExistentPlayerId, "RED")
        
        // Then
        // Sollte keine Exception werfen
        assertNull(PlayerManager.getPlayer(nonExistentPlayerId))
    }
}
