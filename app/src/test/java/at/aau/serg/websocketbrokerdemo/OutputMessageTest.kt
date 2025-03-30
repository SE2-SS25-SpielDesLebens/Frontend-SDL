package at.aau.serg.websocketbrokerdemo.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class OutputMessageTest {

    @Test
    fun `Konstruktor setzt Werte korrekt`() {
        // Given
        val playerName = "Anna"
        val content = "würfelt 6"
        val timestamp = "2023-06-15T10:15:30"

        // When
        val message = OutputMessage(playerName, content, timestamp)

        // Then
        assertEquals(playerName, message.playerName)
        assertEquals(content, message.content)
        assertEquals(timestamp, message.timestamp)
    }

    @Test
    fun `equals und hashCode funktionieren korrekt`() {
        // Given
        val message1 = OutputMessage("Anna", "würfelt 6", "2023-06-15T10:15:30")
        val message2 = OutputMessage("Anna", "würfelt 6", "2023-06-15T10:15:30")
        val message3 = OutputMessage("Bob", "würfelt 4", "2023-06-15T10:16:30")

        // Then
        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
        assertEquals(message1.hashCode(), message2.hashCode())
        assertNotEquals(message1.hashCode(), message3.hashCode())
    }

    @Test
    fun `toString gibt alle Eigenschaften zurück`() {
        // Given
        val message = OutputMessage("Anna", "würfelt 6", "2023-06-15T10:15:30")

        // When
        val result = message.toString()

        // Then
        assertTrue(result.contains("Anna"))
        assertTrue(result.contains("würfelt 6"))
        assertTrue(result.contains("2023-06-15T10:15:30"))
    }

    @Test
    fun `copy erstellt korrekte Kopie`() {
        // Given
        val originalMessage = OutputMessage("Anna", "würfelt 6", "2023-06-15T10:15:30")

        // When
        val copiedMessage = originalMessage.copy()
        val modifiedMessage = originalMessage.copy(content = "würfelt 3")

        // Then
        assertEquals(originalMessage, copiedMessage)
        assertEquals(originalMessage.playerName, modifiedMessage.playerName)
        assertEquals("würfelt 3", modifiedMessage.content)
        assertEquals(originalMessage.timestamp, modifiedMessage.timestamp)
    }

    @Test
    fun `kann mit Gson serialisiert und deserialisiert werden`() {
        // Given
        val originalMessage = OutputMessage("Anna", "würfelt 6", "2023-06-15T10:15:30")
        val gson = Gson()

        // When
        val json = gson.toJson(originalMessage)
        val deserializedMessage = gson.fromJson(json, OutputMessage::class.java)

        // Then
        assertEquals(originalMessage, deserializedMessage)
        assertTrue(json.contains("\"playerName\":\"Anna\""))
        assertTrue(json.contains("\"content\":\"würfelt 6\""))
        assertTrue(json.contains("\"timestamp\":\"2023-06-15T10:15:30\""))
    }
}