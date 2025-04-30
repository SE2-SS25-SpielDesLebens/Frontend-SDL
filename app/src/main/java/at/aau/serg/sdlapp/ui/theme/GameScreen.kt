package at.aau.serg.sdlapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.ui.theme.PlayerModell

@Composable
fun GameScreen() {
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    val playerStats = remember { mutableStateListOf<PlayerModell>() }

    // Spieler aus dem Backend laden
    LaunchedEffect(Unit) {
        try {
            val fetchedPlayers = PlayerRepository.fetchPlayers()
            println("Fetched players: $fetchedPlayers")
            playerStats.clear()
            playerStats.addAll(fetchedPlayers)
        } catch (e: Exception) {
            println("Fehler beim Laden der Spieler: ${e.message}")
        }
    }



    Column(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        Text(
            text = "Spiel des Lebens",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        if (playerStats.isNotEmpty()) {
            PlayerStatsOverlay(playerStats[currentPlayerIndex])
        } else {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }


        Button(
            onClick = { currentPlayerIndex = (currentPlayerIndex + 1) % playerStats.size },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Nächster Spieler")
        }


    }
}
