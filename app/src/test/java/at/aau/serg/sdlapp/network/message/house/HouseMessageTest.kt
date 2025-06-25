package at.aau.serg.sdlapp.network.message.house

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class HouseMessageTest {

    @Test
    fun `create HouseMessage and verify fields`() {
        val house = HouseMessage(
            houseId = 1,
            bezeichnung = "Villa Sonnenschein",
            kaufpreis = 300000,
            verkaufspreisRot = 250000,
            verkaufspreisSchwarz = 280000,
            isTaken = true,
            assignedToPlayerName = "Alice",
            gameId = 42,
            sellPrice = true
        )

        Assertions.assertEquals(1, house.houseId)
        Assertions.assertEquals("Villa Sonnenschein", house.bezeichnung)
        Assertions.assertEquals(300000, house.kaufpreis)
        Assertions.assertEquals(250000, house.verkaufspreisRot)
        Assertions.assertEquals(280000, house.verkaufspreisSchwarz)
        Assertions.assertTrue(house.isTaken)
        Assertions.assertEquals("Alice", house.assignedToPlayerName)
        Assertions.assertEquals(42, house.gameId)
        Assertions.assertTrue(house.sellPrice)
    }

    @Test
    fun `copy HouseMessage with different player and sellPrice`() {
        val original = HouseMessage(2, "Hütte", 100000, 80000, 85000, false, null, 1, false)
        val copy = original.copy(assignedToPlayerName = "Bob", sellPrice = true)

        Assertions.assertEquals("Bob", copy.assignedToPlayerName)
        Assertions.assertTrue(copy.sellPrice)
        Assertions.assertEquals(original.houseId, copy.houseId)
        Assertions.assertNotEquals(original, copy)
    }

    @Test
    fun `house messages with same data should be equal`() {
        val h1 = HouseMessage(3, "Bungalow", 150000, 120000, 130000, false, "PlayerX", 7, false)
        val h2 = HouseMessage(3, "Bungalow", 150000, 120000, 130000, false, "PlayerX", 7, false)

        Assertions.assertEquals(h1, h2)
        Assertions.assertEquals(h1.hashCode(), h2.hashCode())
    }

    @Test
    fun `handle null player name`() {
        val house = HouseMessage(4, "Wohnung", 200000, 180000, 190000, false, null, 99, true)
        Assertions.assertNull(house.assignedToPlayerName)
    }
}
