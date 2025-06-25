package at.aau.serg.sdlapp.ui.PlayerOverlay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.network.viewModels.PlayerViewModel

@Composable
fun AllPlayerStatsScreen(viewModel: PlayerViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadAllPlayers()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        viewModel.allPlayers.forEach { player ->
            PlayerStatsOverlay(player = player)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


