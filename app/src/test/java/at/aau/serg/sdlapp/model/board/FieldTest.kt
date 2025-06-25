package at.aau.serg.sdlapp.model.board

import org.junit.Assert.*
import org.junit.Test

class FieldTest {

    @Test
    fun `Field properties are assigned correctly`() {
        val field = Field(
            index = 1,
            x = 0.25f,
            y = 0.5f,
            nextFields = listOf(2, 3, 4),
            type = FieldType.ZAHLTAG
        )

        assertEquals(1, field.index)
        assertEquals(0.25f, field.x)
        assertEquals(0.5f, field.y)
        assertEquals(listOf(2, 3, 4), field.nextFields)
        assertEquals(FieldType.ZAHLTAG, field.type)
    }

    @Test
    fun `Fields with same values are equal`() {
        val field1 = Field(5, 0.1f, 0.2f, listOf(6, 7), FieldType.FREUND)
        val field2 = Field(5, 0.1f, 0.2f, listOf(6, 7), FieldType.FREUND)

        assertEquals(field1, field2)
        assertEquals(field1.hashCode(), field2.hashCode())
    }

    @Test
    fun `Fields with different values are not equal`() {
        val field1 = Field(5, 0.1f, 0.2f, listOf(6), FieldType.AKTION)
        val field2 = Field(6, 0.1f, 0.2f, listOf(6), FieldType.AKTION)

        assertNotEquals(field1, field2)
    }

    @Test
    fun `Default field type is AKTION`() {
        val field = Field(index = 10, x = 0.0f, y = 0.0f, nextFields = emptyList())
        assertEquals(FieldType.AKTION, field.type)
    }
}
