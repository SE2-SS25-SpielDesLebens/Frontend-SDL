package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class PlayerPositionsMessageTest {

    @Test
    fun `PlayerPositionsMessage assigns all values correctly`() {
        val positions = mapOf("Alice" to 3, "Bob" to 7)
        val timestamp = "2024-06-20T12:00:00Z"

        val message = PlayerPositionsMessage(
            playerPositions = positions,
            timestamp = timestamp
        )

        Assertions.assertEquals(positions, message.playerPositions)
        Assertions.assertEquals(timestamp, message.timestamp)
        Assertions.assertEquals("playerPositions", message.type)
    }

    @Test
    fun `PlayerPositionsMessage equality check`() {
        val positions = mapOf("Alice" to 1)
        val timestamp = "2024-06-20T15:30:00Z"

        val msg1 = PlayerPositionsMessage(positions, timestamp)
        val msg2 = PlayerPositionsMessage(positions, timestamp)

        Assertions.assertEquals(msg1, msg2)
        Assertions.assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun `PlayerPositionsMessage inequality when data differs`() {
        val msg1 = PlayerPositionsMessage(mapOf("A" to 5), "2024-06-20T10:00:00Z")
        val msg2 = PlayerPositionsMessage(mapOf("A" to 6), "2024-06-20T10:00:00Z")

        Assertions.assertNotEquals(msg1, msg2)
    }

    @Test
    fun `Default type is playerPositions`() {
        val msg = PlayerPositionsMessage(mapOf(), "timestamp")
        Assertions.assertEquals("playerPositions", msg.type)
    }
}
