package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class PlayerListMessageTest {
    @Test
    fun `constructor sets all values correctly`() {
        val msg = PlayerListMessage(type = "players", playerList = listOf("1","2","3"), timestamp = "2024-05-31T12:00:00Z")
        Assertions.assertEquals("players", msg.type)
        Assertions.assertEquals(listOf("1","2","3"), msg.playerList)
        Assertions.assertEquals("2024-05-31T12:00:00Z", msg.timestamp)
    }

    @Test
    fun `default values are correct`() {
        val msg = PlayerListMessage()
        Assertions.assertEquals("players", msg.type)
        Assertions.assertTrue(msg.playerList.isEmpty())
        Assertions.assertEquals("", msg.timestamp)
    }

    @Test
    fun `copy creates identical but separate object`() {
        val original = PlayerListMessage("players", listOf("1","2"), "t")
        val copy = original.copy()
        Assertions.assertEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }
}
