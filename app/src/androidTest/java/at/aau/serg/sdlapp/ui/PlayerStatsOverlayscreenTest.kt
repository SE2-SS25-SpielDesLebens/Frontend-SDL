package at.aau.serg.sdlapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.sdlapp.model.player.PlayerModell
import at.aau.serg.sdlapp.network.viewModels.PlayerViewModel
import at.aau.serg.sdlapp.ui.PlayerOverlay.PlayerStatsOverlayScreen
import org.junit.Rule
import org.junit.Test

class PlayerStatsOverlayScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsLoadingIndicator_whenPlayerIsNull() {
        val viewModel = PlayerViewModel().apply {
            player = null
        }

        composeTestRule.setContent {
            PlayerStatsOverlayScreen(playerId = "999", viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Aktueller Player: NULL").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertExists()
    }

    @Test
    fun showsPlayer_whenDataIsLoaded() {
        val viewModel = PlayerViewModel().apply {
            player = PlayerModell("5", 9000, 1000, 2000, 0, false, false)
        }

        composeTestRule.setContent {
            PlayerStatsOverlayScreen(playerId = "5", viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("9k").assertExists()
    }
}
