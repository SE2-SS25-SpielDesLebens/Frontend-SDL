package at.aau.serg.websocketbrokerdemo.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class StompMessageTest {

    @Test
    fun `Konstruktor setzt Werte korrekt mit allen Parametern`() {
        // Given
        val playerName = "Anna"
        val action = "würfelt 6"
        val messageText = "Hallo an alle!"

        // When
        val message = StompMessage(playerName, action, messageText)

        // Then
        assertEquals(playerName, message.playerName)
        assertEquals(action, message.action)
        assertEquals(messageText, message.messageText)
    }

    @Test
    fun `Konstruktor setzt Standardwerte für optionale Parameter`() {
        // Given/When
        val message = StompMessage("Anna")

        // Then
        assertEquals("Anna", message.playerName)
        assertNull(message.action)
        assertNull(message.messageText)
    }

    @Test
    fun `Konstruktor setzt nur action Parameter`() {
        // Given/When
        val message = StompMessage("Anna", "würfelt 6")

        // Then
        assertEquals("Anna", message.playerName)
        assertEquals("würfelt 6", message.action)
        assertNull(message.messageText)
    }

    @Test
    fun `Konstruktor setzt nur messageText Parameter`() {
        // Given/When
        val message = StompMessage("Anna", messageText = "Hallo an alle!")

        // Then
        assertEquals("Anna", message.playerName)
        assertNull(message.action)
        assertEquals("Hallo an alle!", message.messageText)
    }

    @Test
    fun `equals und hashCode funktionieren korrekt`() {
        // Given
        val message1 = StompMessage("Anna", "würfelt 6", "Hallo an alle!")
        val message2 = StompMessage("Anna", "würfelt 6", "Hallo an alle!")
        val message3 = StompMessage("Bob", "würfelt 4", "Hi")

        // Then
        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
        assertEquals(message1.hashCode(), message2.hashCode())
        assertNotEquals(message1.hashCode(), message3.hashCode())
    }

    @Test
    fun `toString gibt alle Eigenschaften zurück`() {
        // Given
        val message = StompMessage("Anna", "würfelt 6", "Hallo an alle!")

        // When
        val result = message.toString()

        // Then
        assertTrue(result.contains("Anna"))
        assertTrue(result.contains("würfelt 6"))
        assertTrue(result.contains("Hallo an alle!"))
    }

    @Test
    fun `copy erstellt korrekte Kopie`() {
        // Given
        val originalMessage = StompMessage("Anna", "würfelt 6", "Hallo an alle!")

        // When
        val copiedMessage = originalMessage.copy()
        val modifiedMessage = originalMessage.copy(action = "würfelt 3")
        val modifiedMessage2 = originalMessage.copy(messageText = "Neuer Text")

        // Then
        assertEquals(originalMessage, copiedMessage)
        assertEquals(originalMessage.playerName, modifiedMessage.playerName)
        assertEquals("würfelt 3", modifiedMessage.action)
        assertEquals(originalMessage.messageText, modifiedMessage.messageText)
        assertEquals("Neuer Text", modifiedMessage2.messageText)
    }

    @Test
    fun `kann mit Gson serialisiert und deserialisiert werden`() {
        // Given
        val originalMessage = StompMessage("Anna", "würfelt 6", "Hallo an alle!")
        val gson = Gson()

        // When
        val json = gson.toJson(originalMessage)
        val deserializedMessage = gson.fromJson(json, StompMessage::class.java)

        // Then
        assertEquals(originalMessage, deserializedMessage)
        assertTrue(json.contains("\"playerName\":\"Anna\""))
        assertTrue(json.contains("\"action\":\"würfelt 6\""))
        assertTrue(json.contains("\"messageText\":\"Hallo an alle!\""))
    }

    @Test
    fun `kann mit Gson serialisiert und deserialisiert werden mit Nullwerten`() {
        // Given
        val originalMessage = StompMessage("Anna")
        val gson = Gson()

        // When
        val json = gson.toJson(originalMessage)
        val deserializedMessage = gson.fromJson(json, StompMessage::class.java)

        // Then
        assertEquals(originalMessage, deserializedMessage)
        assertTrue(json.contains("\"playerName\":\"Anna\""))
        assertNull(deserializedMessage.action)
        assertNull(deserializedMessage.messageText)
    }
}