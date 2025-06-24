package at.aau.serg.sdlapp.network

import at.aau.serg.sdlapp.network.message.StompMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StompMessageTest {

    @Test
    fun testKonstruktor() {
        val message = StompMessage("Spieler1", "laufen", "Testnachricht")

        assertEquals("Spieler1", message.playerName)
        assertEquals("laufen", message.action)
        assertEquals("Testnachricht", message.messageText)
    }

    @Test
    fun testEquals() {
        val msg1 = StompMessage("Spieler1", "laufen", "Hi")
        val msg2 = StompMessage("Spieler1", "laufen", "Hi")
        val msg3 = StompMessage("Spieler2", "springen", "Hallo")

        assertEquals(msg1, msg2)
        assertNotEquals(msg1, msg3)
    }

    @Test
    fun testHashCode() {
        val msg1 = StompMessage("Spieler1", "laufen", "Hi")
        val msg2 = StompMessage("Spieler1", "laufen", "Hi")

        assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun testToString() {
        val message = StompMessage("Spieler1", "gehen", "Hallo")
        val text = message.toString()

        assertTrue(text.contains("Spieler1"))
        assertTrue(text.contains("gehen"))
        assertTrue(text.contains("Hallo"))
    }

    @Test
    fun testCopy() {
        val original = StompMessage("Spieler1", "gehen", "Hallo")

        val copy1 = original.copy(playerName = "Spieler2")
        assertEquals("Spieler2", copy1.playerName)
        assertEquals("gehen", copy1.action)
        assertEquals("Hallo", copy1.messageText)

        val copy2 = original.copy(action = "springen")
        assertEquals("Spieler1", copy2.playerName)
        assertEquals("springen", copy2.action)
        assertEquals("Hallo", copy2.messageText)

        val copy3 = original.copy(messageText = "Bye")
        assertEquals("Spieler1", copy3.playerName)
        assertEquals("gehen", copy3.action)
        assertEquals("Bye", copy3.messageText)
    }

    @Test
    fun testComponentFunctions() {
        val message = StompMessage("Spieler1", "gehen", "Hallo")

        assertEquals("Spieler1", message.component1())
        assertEquals("gehen", message.component2())
        assertEquals("Hallo", message.component3())
    }
    @Test
    fun testMinimalConstructor() {
        val msg = StompMessage("Spieler1")

        assertEquals("Spieler1", msg.playerName)
        assertNull(msg.action)
        assertNull(msg.messageText)
    }

    @Test
    fun testDeconstruction() {
        val message = StompMessage("Spieler1", "gehen", "Hallo")

        val (spieler, aktion, text) = message

        assertEquals("Spieler1", spieler)
        assertEquals("gehen", aktion)
        assertEquals("Hallo", text)
    }
}
