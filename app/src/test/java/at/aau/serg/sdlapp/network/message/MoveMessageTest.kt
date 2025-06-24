package at.aau.serg.sdlapp.network.message

import at.aau.serg.sdlapp.model.board.FieldType
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class MoveMessageTest {

    @Test
    fun `MoveMessage assigns values correctly`() {
        val message = MoveMessage(
            playerName = "Alice",
            fieldIndex = 7,
            typeString = "ZAHLTAG",
            timestamp = "2025-06-20T10:00:00Z",
            nextPossibleFields = listOf(8, 9)
        )

        Assertions.assertEquals("Alice", message.playerName)
        Assertions.assertEquals(7, message.fieldIndex)
        Assertions.assertEquals("ZAHLTAG", message.typeString)
        Assertions.assertEquals("2025-06-20T10:00:00Z", message.timestamp)
        Assertions.assertEquals(listOf(8, 9), message.nextPossibleFields)
        Assertions.assertEquals("Alice", message.playerId)
        Assertions.assertEquals(FieldType.ZAHLTAG, message.fieldType)
    }

    @Test
    fun `fieldType falls back to AKTION on invalid typeString`() {
        val message = MoveMessage(
            playerName = "Bob",
            fieldIndex = 5,
            typeString = "UNKNOWN_TYPE"
        )

        Assertions.assertEquals(FieldType.AKTION, message.fieldType)
    }

    @Test
    fun `default timestamp is null and nextPossibleFields is empty`() {
        val message = MoveMessage(
            playerName = "Eve",
            fieldIndex = 3,
            typeString = "FREUND"
        )

        Assertions.assertNull(message.timestamp)
        Assertions.assertTrue(message.nextPossibleFields.isEmpty())
    }

    @Test
    fun `MoveMessages with same content are equal`() {
        val msg1 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))
        val msg2 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))

        Assertions.assertEquals(msg1, msg2)
        Assertions.assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun `MoveMessages with different content are not equal`() {
        val msg1 = MoveMessage("A", 1, "AKTION", "ts", listOf(2))
        val msg2 = MoveMessage("A", 1, "ZAHLTAG", "ts", listOf(2))

        Assertions.assertNotEquals(msg1, msg2)
    }
}
