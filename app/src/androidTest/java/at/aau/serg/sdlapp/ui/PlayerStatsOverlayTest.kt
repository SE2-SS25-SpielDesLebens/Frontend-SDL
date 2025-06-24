package at.aau.serg.sdlapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class PlayerStatsOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOverlayDisplaysCorrectly() {
        val player = PlayerModell(
            id = "Spieler#1",
            money = 10000,
            investments = 3000,
            salary = 5000,
            children = 2,
            education = true,
            relationship = false
        )

        composeTestRule.setContent {
            MaterialTheme {
                PlayerStatsOverlay(player = player)
            }
        }

        // 🧪 Check ID and money
        composeTestRule.onNodeWithText("Spieler#1").assertExists()
        composeTestRule.onNodeWithText("10k").assertExists()

        // 🧪 Check children (❤️), education (📘) and investments (💰)
        composeTestRule.onNodeWithText("❤️").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()

        composeTestRule.onNodeWithText("📘").assertExists()
        composeTestRule.onNodeWithText("✓").assertExists()

        composeTestRule.onNodeWithText("💰").assertExists()
        composeTestRule.onNodeWithText("3k").assertExists()
    }

    @Test
    fun testOverlayEducationFalseDisplaysCross() {
        val player = PlayerModell(
            id = "Spieler#2",
            money = 8000,
            investments = 0,
            salary = 4000,
            children = 0,
            education = false,
            relationship = false
        )

        composeTestRule.setContent {
            MaterialTheme {
                PlayerStatsOverlay(player = player)
            }
        }

        // 📘 should display ✗ if no education
        composeTestRule.onNodeWithText("📘").assertExists()
        composeTestRule.onNodeWithText("✗").assertExists()
    }
}
