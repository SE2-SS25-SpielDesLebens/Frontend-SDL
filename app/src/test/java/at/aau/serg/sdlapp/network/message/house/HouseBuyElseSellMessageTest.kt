package at.aau.serg.sdlapp.network.message.house

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HouseBuyElseSellMessageTest {

    @Test
    fun `create message and verify fields`() {
        val message = HouseBuyElseSellMessage(
            playerID = "player123",
            gameId = 42,
            buyElseSell = true
        )

        Assertions.assertEquals("player123", message.playerID)
        Assertions.assertEquals(42, message.gameId)
        Assertions.assertTrue(message.buyElseSell)
    }

    @Test
    fun `copy creates equal but independent instance`() {
        val original = HouseBuyElseSellMessage("playerA", 1, false)
        val copy = original.copy(buyElseSell = true)

        Assertions.assertEquals("playerA", copy.playerID)
        Assertions.assertEquals(1, copy.gameId)
        Assertions.assertTrue(copy.buyElseSell)
        Assertions.assertNotEquals(original, copy)
    }
}
