package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.Test

class OutputMessageTest {

    @Test
    fun `OutputMessage assigns all values correctly`() {
        val message = OutputMessage(
            playerName = "Alice",
            content = "Hello, world!",
            timestamp = "2025-06-20T12:34:56Z"
        )

        assertEquals("Alice", message.playerName)
        assertEquals("Hello, world!", message.content)
        assertEquals("2025-06-20T12:34:56Z", message.timestamp)
    }

    @Test
    fun `OutputMessages with same values are equal`() {
        val msg1 = OutputMessage("Bob", "Message", "2025-06-20T15:00:00Z")
        val msg2 = OutputMessage("Bob", "Message", "2025-06-20T15:00:00Z")

        assertEquals(msg1, msg2)
        assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun `OutputMessages with different values are not equal`() {
        val msg1 = OutputMessage("Carol", "Hi", "2025-06-20T12:00:00Z")
        val msg2 = OutputMessage("Carol", "Bye", "2025-06-20T12:00:00Z")

        assertNotEquals(msg1, msg2)
    }
}
