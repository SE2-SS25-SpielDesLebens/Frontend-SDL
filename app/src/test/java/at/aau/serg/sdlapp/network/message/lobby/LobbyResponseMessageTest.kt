package at.aau.serg.sdlapp.network.message.lobby

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class LobbyResponseMessageTest {
    @Test
    fun `constructor sets all values correctly`() {
        val message = LobbyResponseMessage(
            lobbyId = "abc123",
            playerName = "Anna",
            isSuccessful = true,
            message = "Lobby joined"
        )
        Assertions.assertEquals("abc123", message.lobbyId)
        Assertions.assertEquals("Anna", message.playerName)
        Assertions.assertTrue(message.isSuccessful)
        Assertions.assertEquals("Lobby joined", message.message)
    }

    @Test
    fun `copy creates an identical but separate object`() {
        val original = LobbyResponseMessage("abc123", "Anna", true, "Lobby joined")
        val copy = original.copy()
        Assertions.assertEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun `toString formats correctly`() {
        val message = LobbyResponseMessage("abc123", "Anna", true, "Lobby joined")
        val expected = "LobbyResponseMessage(lobbyId=abc123, playerName=Anna, isSuccessful=true, message=Lobby joined)"
        Assertions.assertEquals(expected, message.toString())
    }
}
