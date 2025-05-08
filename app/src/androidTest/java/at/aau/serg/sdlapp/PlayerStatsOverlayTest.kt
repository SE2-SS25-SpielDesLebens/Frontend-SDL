package at.aau.serg.sdlapp

import androidx.compose.ui.test.*
import at.aau.serg.sdlapp.ui.theme.PlayerModell
import at.aau.serg.sdlapp.ui.theme.PlayerStatsOverlay
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PlayerStatsOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPlayerStatsOverlayRendering() {
        val player = PlayerModell(
            id = 1,
            name = "Spieler #1",
            money = 10000,
            investments = 2000,
            salary = 5000,
            children = 2,
            education = "Bachelor",
            relationship = "Single",
            career = "Kellner",
            jobID = 101,
            houseID = 202
        )

        composeTestRule.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color.Blue,
                    secondary = Color.Green
                ),
                typography = Typography()
            ) {
                PlayerStatsOverlay(player = player)
            }
        }
        Thread.sleep(10000)
        // Alle StatCard-Bezeichnungen prüfen
        composeTestRule.onNodeWithText("Spieler #1").assertExists()
        composeTestRule.onNodeWithText("💰 Geld").assertExists()
        composeTestRule.onNodeWithText("💼 Gehalt").assertExists()
        composeTestRule.onNodeWithText("🧑‍🍳 Beruf").assertExists()
        composeTestRule.onNodeWithText("🎓 Bildung").assertExists()
        composeTestRule.onNodeWithText("❤️ Beziehung").assertExists()
        composeTestRule.onNodeWithText("📈 Investitionen").assertExists()
        composeTestRule.onNodeWithText("👶 Kinder").assertExists()
        composeTestRule.onNodeWithText("🆔 Job-ID").assertExists()
        composeTestRule.onNodeWithText("🏠 Haus-ID").assertExists()
    }
}

