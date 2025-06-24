package at.aau.serg.sdlapp.network.message.lobby

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class LobbyUpdateMessageTest {
    @Test
    fun `constructor sets all values correctly`() {
        val message = LobbyUpdateMessage(
            player1 = "Alice",
            player2 = "Bob",
            player3 = "Charlie",
            player4 = "Dana",
            isStarted = true
        )
        Assertions.assertEquals("Alice", message.player1)
        Assertions.assertEquals("Bob", message.player2)
        Assertions.assertEquals("Charlie", message.player3)
        Assertions.assertEquals("Dana", message.player4)
        Assertions.assertTrue(message.isStarted)
    }

    @Test
    fun `copy creates identical but separate object`() {
        val original = LobbyUpdateMessage("A", "B", "C", "D", false)
        val copy = original.copy()
        Assertions.assertEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun `equality and inequality work`() {
        val m1 = LobbyUpdateMessage("A", "B", "C", "D", false)
        val m2 = LobbyUpdateMessage("A", "B", "C", "D", false)
        val m3 = LobbyUpdateMessage("A", "B", "C", "D", true)
        Assertions.assertEquals(m1, m2)
        Assertions.assertNotEquals(m1, m3)
    }
}
