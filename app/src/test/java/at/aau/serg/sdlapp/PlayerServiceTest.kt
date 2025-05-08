import androidx.compose.ui.graphics.Color
import at.aau.serg.sdlapp.ui.theme.PlayerModell
import at.aau.serg.sdlapp.ui.theme.getMoneyColor
import at.aau.serg.sdlapp.ui.theme.getSalaryColor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerServiceTest {

    @Test
    fun `test money calculation`() {
        val player = PlayerModell(id = 1, name = "Hans", money = 10000, investments = 1000, salary = 5000, children = 0, education = "Bachelor", relationship = "Single", career = "Kellner")
        val expectedMoney = player.money + player.investments
        assertEquals(11000, expectedMoney)
    }

    @Test
    fun `test player career`() {
        val player = PlayerModell(id = 1, name = "Hans", money = 10000, investments = 0, salary = 5000, children = 0, education = "Bachelor", relationship = "Single", career = "Kellner")

        assertEquals("Kellner", player.career)
    }

    @Test
    fun `test player children count`() {
        val player = PlayerModell(id = 1, name = "Eva", money = 15000, investments = 2000, salary = 6000, children = 3, education = "Master", relationship = "Verheiratet", career = "Koch")

        assertEquals(3, player.children)
    }

    @Test
    fun `test player education`() {
        val player = PlayerModell(id = 2, name = "Lena", money = 8000, investments = 1000, salary = 3000, children = 1, education = "Bachelor", relationship = "Verheiratet", career = "Lehrerin")

        assertEquals("Bachelor", player.education)
    }

    @Test
    fun `test player relationship status`() {
        val player = PlayerModell(id = 3, name = "Max", money = 4000, investments = 0, salary = 2500, children = 0, education = "Abitur", relationship = "Single", career = "Student")

        assertEquals("Single", player.relationship)
    }

    @Test
    fun `test player career profession`() {
        val player = PlayerModell(id = 4, name = "John", money = 10000, investments = 1000, salary = 4000, children = 1, education = "Master", relationship = "Verheiratet", career = "Ingenieur")

        assertEquals("Ingenieur", player.career)
    }
}
