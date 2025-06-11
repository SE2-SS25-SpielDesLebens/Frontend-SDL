package at.aau.serg.sdlapp.network.viewModels

import at.aau.serg.sdlapp.model.player.PlayerManager
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test


class GameResultViewModelTest {

    @Before
    fun setup() {
        PlayerManager.clearPlayers()
        PlayerManager.addPlayer("1", "Alice")
        PlayerManager.addPlayer("2", "Bob")
        PlayerManager.getPlayer("1")?.apply {
            money = 2000
            investments = 500
            salary = 1000
            children = 2
            hasEducation = true
        }
        PlayerManager.getPlayer("2")?.apply {
            money = 1000
            investments = 1000
            salary = 2000
            children = 3
            hasEducation = false
        }
    }

    @After
    fun tearDown() {
        PlayerManager.clearPlayers()
    }

    @Test
    fun `players are sorted by total wealth`() {
        val vm = GameResultViewModel()
        val sorted = vm.sortedPlayers
        assertEquals("Alice", sorted[0].name)
        assertEquals("Bob", sorted[1].name)
    }

    @Test
    fun `most children winner is Bob`() {
        val vm = GameResultViewModel()
        assertEquals("Bob", vm.mostChildren)
    }

    @Test
    fun `top investor is Bob`() {
        val vm = GameResultViewModel()
        assertEquals("Bob", vm.topInvestor)
    }

    @Test
    fun `highest salary is Bob`() {
        val vm = GameResultViewModel()
        assertEquals("Bob", vm.highestSalary)
    }

    @Test
    fun `academics returns only Alice`() {
        val vm = GameResultViewModel()
        assertEquals("Alice", vm.academics)
    }

    @Test
    fun `clearing players results in empty list`() {
        val vm = GameResultViewModel()
        vm.clearGameData()
        assertEquals(0, PlayerManager.getAllPlayers().size)
    }
}
