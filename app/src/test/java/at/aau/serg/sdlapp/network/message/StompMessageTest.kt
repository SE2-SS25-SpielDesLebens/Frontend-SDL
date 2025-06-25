package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class StompMessageTest {

    @Test
    fun `StompMessage assigns all properties correctly`() {
        val message = StompMessage(
            playerName = "Alice",
            action = "JOIN",
            messageText = "Hello",
            gameId = "42"
        )

        Assertions.assertEquals("Alice", message.playerName)
        Assertions.assertEquals("JOIN", message.action)
        Assertions.assertEquals("Hello", message.messageText)
        Assertions.assertEquals("42", message.gameId)
    }

    @Test
    fun `StompMessage uses default values when optional parameters are omitted`() {
        val message = StompMessage(playerName = "Bob")

        Assertions.assertEquals("Bob", message.playerName)
        Assertions.assertNull(message.action)
        Assertions.assertNull(message.messageText)
        Assertions.assertNull(message.gameId)
    }

    @Test
    fun `StompMessages with same content are equal`() {
        val msg1 = StompMessage("Charlie", "MOVE", "Let's go!", "99")
        val msg2 = StompMessage("Charlie", "MOVE", "Let's go!", "99")

        Assertions.assertEquals(msg1, msg2)
        Assertions.assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun `StompMessages with different content are not equal`() {
        val msg1 = StompMessage("Charlie", "MOVE", "Go", "1")
        val msg2 = StompMessage("Charlie", "WAIT", "Stop", "1")

        Assertions.assertNotEquals(msg1, msg2)
    }
}
