package at.aau.serg.sdlapp.network.message

import at.aau.serg.sdlapp.model.board.FieldType
import org.junit.Assert.*
import org.junit.Test

class MoveMessageTest {

    @Test
    fun moveMessageAssignsValuesCorrectly() {
        val message = MoveMessage(
            playerName = "Alice",
            fieldIndex = 7,
            typeString = "ZAHLTAG",
            timestamp = "2025-06-20T10:00:00Z",
            nextPossibleFields = listOf(8, 9)
        )

        assertEquals("Alice", message.playerName)
        assertEquals(7, message.fieldIndex)
        assertEquals("ZAHLTAG", message.typeString)
        assertEquals("2025-06-20T10:00:00Z", message.timestamp)
        assertEquals(listOf(8, 9), message.nextPossibleFields)
        assertEquals("Alice", message.playerId)
        assertEquals(FieldType.ZAHLTAG, message.fieldType)
    }

    @Test
    fun fieldTypeFallsBackToAKTIONOnInvalidTypeString() {
        val message = MoveMessage("Bob", 5, "UNKNOWN_TYPE")
        assertEquals(FieldType.AKTION, message.fieldType)
    }

    @Test
    fun defaultTimestampIsNullAndNextPossibleFieldsEmpty() {
        val message = MoveMessage("Eve", 3, "FREUND")
        assertNull(message.timestamp)
        assertTrue(message.nextPossibleFields.isEmpty())
    }

    @Test
    fun moveMessagesWithSameContentAreEqual() {
        val msg1 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))
        val msg2 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))
        assertEquals(msg1, msg2)
        assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun moveMessagesWithDifferentContentAreNotEqual() {
        val msg1 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))
        val msg2 = MoveMessage("A", 1, "ZAHLTAG", "ts", listOf(2))
        assertNotEquals(msg1, msg2)
    }

    @Test
    fun playerIdIsDerivedFromPlayerName() {
        val message = MoveMessage("Carla", 10, "AKTION")
        assertEquals("Carla", message.playerId)
    }

    @Test
    fun fieldTypeIsCorrectForAllEnumValues() {
        FieldType.entries.forEach { type ->
            val message = MoveMessage("X", 0, type.name)
            assertEquals(type, message.fieldType)
        }
    }

    @Test
    fun emptyTypeStringDefaultsToAktion() {
        val message = MoveMessage("X", 0, "")
        assertEquals(FieldType.AKTION, message.fieldType)
    }
}
