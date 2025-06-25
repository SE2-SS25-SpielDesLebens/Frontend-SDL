package at.aau.serg.sdlapp.network

import at.aau.serg.sdlapp.network.message.OutputMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class OutputMessageTest {

    @Test
    fun testKonstruktor() {
        val message = OutputMessage("Spieler1", "Hallo Welt", "12:30:45")

        assertEquals("Spieler1", message.playerName)
        assertEquals("Hallo Welt", message.content)
        assertEquals("12:30:45", message.timestamp)
    }

    @Test
    fun testEquals() {
        val message1 = OutputMessage("Spieler1", "Hallo", "12:00:00")
        val message2 = OutputMessage("Spieler1", "Hallo", "12:00:00")
        val message3 = OutputMessage("Spieler2", "Hallo", "12:00:00")

        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
    }

    @Test
    fun testHashCode() {
        val message1 = OutputMessage("Spieler1", "Hallo", "12:00:00")
        val message2 = OutputMessage("Spieler1", "Hallo", "12:00:00")

        assertEquals(message1.hashCode(), message2.hashCode())
    }

    @Test
    fun testToString() {
        val message = OutputMessage("Spieler1", "Hallo", "12:00:00")
        val stringRepresentation = message.toString()

        assertTrue(stringRepresentation.contains("Spieler1"))
        assertTrue(stringRepresentation.contains("Hallo"))
        assertTrue(stringRepresentation.contains("12:00:00"))
    }

    @Test
    fun testCopy() {
        val original = OutputMessage("Spieler1", "Hallo", "12:00:00")

        val kopie1 = original.copy(playerName = "Spieler2")
        assertEquals("Spieler2", kopie1.playerName)
        assertEquals("Hallo", kopie1.content)
        assertEquals("12:00:00", kopie1.timestamp)

        val kopie2 = original.copy(content = "Tschüss")
        assertEquals("Spieler1", kopie2.playerName)
        assertEquals("Tschüss", kopie2.content)
        assertEquals("12:00:00", kopie2.timestamp)

        val kopie3 = original.copy(timestamp = "13:00:00")
        assertEquals("Spieler1", kopie3.playerName)
        assertEquals("Hallo", kopie3.content)
        assertEquals("13:00:00", kopie3.timestamp)
    }

    @Test
    fun testComponentFunctions() {
        val message = OutputMessage("Spieler1", "Hallo", "12:00:00")

        assertEquals("Spieler1", message.component1())
        assertEquals("Hallo", message.component2())
        assertEquals("12:00:00", message.component3())
    }

    @Test
    fun testDeconstruction() {
        val message = OutputMessage("Spieler1", "Hallo", "12:00:00")

        val (spieler, inhalt, zeit) = message

        assertEquals("Spieler1", spieler)
        assertEquals("Hallo", inhalt)
        assertEquals("12:00:00", zeit)
    }
}