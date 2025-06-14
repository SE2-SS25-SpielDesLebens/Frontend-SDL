package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.Test

class PlayerListMessageTest {
    @Test
    fun `constructor sets all values correctly`() {
        val msg = PlayerListMessage(type = "players", playerList = listOf("1","2","3"), timestamp = "2024-05-31T12:00:00Z")
        assertEquals("players", msg.type)
        assertEquals(listOf("1","2","3"), msg.playerList)
        assertEquals("2024-05-31T12:00:00Z", msg.timestamp)
    }

    @Test
    fun `default values are correct`() {
        val msg = PlayerListMessage()
        assertEquals("players", msg.type)
        assertTrue(msg.playerList.isEmpty())
        assertEquals("", msg.timestamp)
    }

    @Test
    fun `copy creates identical but separate object`() {
        val original = PlayerListMessage("players", listOf("1","2"), "t")
        val copy = original.copy()
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }
}
