package at.aau.serg.sdlapp.network.message.job

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JobRequestMessageTest {

    @Test
    fun `create JobRequestMessage and verify fields`() {
        val msg = JobRequestMessage(
            playerName = "Alice",
            gameId = 1,
            jobId = 10
        )

        Assertions.assertEquals("Alice", msg.playerName)
        Assertions.assertEquals(1, msg.gameId)
        Assertions.assertEquals(10, msg.jobId)
    }

    @Test
    fun `jobId can be null`() {
        val msg = JobRequestMessage(
            playerName = "Bob",
            gameId = 2,
            jobId = null
        )

        Assertions.assertEquals("Bob", msg.playerName)
        Assertions.assertEquals(2, msg.gameId)
        Assertions.assertNull(msg.jobId)
    }

    @Test
    fun `copy JobRequestMessage with new jobId`() {
        val original = JobRequestMessage("Clara", 3, 5)
        val copy = original.copy(jobId = 6)

        Assertions.assertEquals("Clara", copy.playerName)
        Assertions.assertEquals(3, copy.gameId)
        Assertions.assertEquals(6, copy.jobId)
        Assertions.assertNotEquals(original, copy)
    }

    @Test
    fun `equal messages should be equal`() {
        val m1 = JobRequestMessage("Dave", 4, 7)
        val m2 = JobRequestMessage("Dave", 4, 7)

        Assertions.assertEquals(m1, m2)
        Assertions.assertEquals(m1.hashCode(), m2.hashCode())
    }
}
