package at.aau.serg.sdlapp.network.message.lobby

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class LobbyRequestMessageTest {
    @Test
    fun `constructor sets playerName correctly`() {
        val request = LobbyRequestMessage(playerName = "Anna")
        Assertions.assertEquals("Anna", request.playerName)
    }

    @Test
    fun `copy creates an identical but separate object`() {
        val original = LobbyRequestMessage("Anna")
        val copy = original.copy()
        Assertions.assertEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun `toString formats correctly`() {
        val request = LobbyRequestMessage("Anna")
        val expected = "LobbyRequestMessage(playerName=Anna)"
        Assertions.assertEquals(expected, request.toString())
    }
}
