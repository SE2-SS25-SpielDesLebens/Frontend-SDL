package at.aau.serg.sdlapp.ui.PlayerOverlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.sdlapp.network.viewModels.PlayerViewModel

@Composable
fun PlayerStatsOverlayScreen(
    playerId: String,
    viewModel: PlayerViewModel = viewModel()
) {
    // Trigger reload jedes Mal, wenn playerId oder triggerState sich ändern
    LaunchedEffect(playerId) {
        println("📡 Lade Player in PlayerStatsOverlayScreen mit ID: $playerId")
        viewModel.loadPlayer(playerId)
    }

    // Zeige Overlay wenn Spieler geladen ist
    viewModel.player?.let { player ->
        println("🎉 Spieler geladen: ${player.id} mit Geld: ${player.money}")
        PlayerStatsOverlay(player = player)
    } ?: Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        println("⌛ Spieler wird noch geladen...")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Lade Spieler...")
        }
    }
}


