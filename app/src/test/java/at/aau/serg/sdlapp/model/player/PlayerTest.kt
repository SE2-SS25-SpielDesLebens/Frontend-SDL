package at.aau.serg.sdlapp.model.player

import at.aau.serg.sdlapp.R
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {

    @Test
    fun `test player default field index is zero`() {
        val player = Player(id = "1", name = "Alice")
        assertEquals(0, player.currentFieldIndex)
    }

    @Test
    fun `test car color is assigned correctly`() {
        val player1 = Player("0", "Zero")
        val player2 = Player("1", "One")
        val player3 = Player("2", "Two")
        val player4 = Player("3", "Three")

        assertEquals(CarColor.BLUE, player1.color)
        assertEquals(CarColor.GREEN, player2.color)
        assertEquals(CarColor.RED, player3.color)
        assertEquals(CarColor.YELLOW, player4.color)
    }

    @Test
    fun `test car image resource for each color`() {
        val bluePlayer = Player("0", "Blue")
        val greenPlayer = Player("1", "Green")
        val redPlayer = Player("2", "Red")
        val yellowPlayer = Player("3", "Yellow")

        assertEquals(R.drawable.car_blue_0, bluePlayer.getCarImageResource())
        assertEquals(R.drawable.car_green_0, greenPlayer.getCarImageResource())
        assertEquals(R.drawable.car_red_0, redPlayer.getCarImageResource())
        assertEquals(R.drawable.car_yellow_0, yellowPlayer.getCarImageResource())
    }
}
