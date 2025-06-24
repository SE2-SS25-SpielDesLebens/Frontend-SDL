package at.aau.serg.sdlapp

import at.aau.serg.sdlapp.ui.PlayerModell
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerServiceTest {

    @Test
    fun `player should correctly calculate money plus investments`() {
        val player = PlayerModell(
            id = "1",
            money = 10000,
            investments = 1000,
            salary = 5000,
            children = 0,
            education = true,
            relationship = false
        )
        val total = player.money + player.investments
        assertEquals(11000, total)
    }

    @Test
    fun `player should have correct number of children`() {
        val player = PlayerModell(
            id = "2",
            money = 15000,
            investments = 2000,
            salary = 6000,
            children = 3,
            education = true,
            relationship = true
        )
        assertEquals(3, player.children)
    }

    @Test
    fun `education status should be true when educated`() {
        val player = PlayerModell(
            id = "3",
            money = 8000,
            investments = 1000,
            salary = 3000,
            children = 1,
            education = true,
            relationship = true
        )
        assertTrue(player.education)
    }

    @Test
    fun `relationship status should be false when not in relationship`() {
        val player = PlayerModell(
            id = "4",
            money = 4000,
            investments = 0,
            salary = 2500,
            children = 0,
            education = false,
            relationship = false
        )
        assertFalse(player.relationship)
    }

    @Test
    fun `player should correctly add salary to money`() {
        val player = PlayerModell(
            id = "5",
            money = 5000,
            investments = 0,
            salary = 2000,
            children = 2,
            education = false,
            relationship = true
        )
        val futureMoney = player.money + player.salary
        assertEquals(7000, futureMoney)
    }

    // 🆕 Neue Tests

    @Test
    fun `total wealth should include money, investments, and salary`() {
        val player = PlayerModell(
            id = "6",
            money = 12000,
            investments = 3000,
            salary = 4000,
            children = 1,
            education = true,
            relationship = false
        )
        val totalWealth = player.money + player.investments + player.salary
        assertEquals(19000, totalWealth)
    }

    @Test
    fun `player with education should receive bonus in logic`() {
        val player = PlayerModell(
            id = "7",
            money = 10000,
            investments = 5000,
            salary = 2000,
            children = 0,
            education = true,
            relationship = true
        )
        val bonus = if (player.education) 1000 else 0
        val total = player.money + bonus
        assertEquals(11000, total)
    }

    @Test
    fun `negative money and investments should be supported`() {
        val player = PlayerModell(
            id = "8",
            money = -500,
            investments = -1000,
            salary = 2000,
            children = 0,
            education = false,
            relationship = false
        )
        val netWorth = player.money + player.investments
        assertEquals(-1500, netWorth)
    }

    @Test
    fun `zero values should not break logic`() {
        val player = PlayerModell(
            id = "9",
            money = 0,
            investments = 0,
            salary = 0,
            children = 0,
            education = false,
            relationship = false
        )
        assertEquals(0, player.money + player.investments + player.salary)
        assertFalse(player.education)
        assertFalse(player.relationship)
    }
}
