package at.aau.serg.sdlapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.model.player.PlayerModell
import at.aau.serg.sdlapp.ui.PlayerOverlay.PlayerStatsOverlay
import kotlinx.coroutines.delay

class PlayerStatsActivity : ComponentActivity() {

    private lateinit var playerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerId = intent.getStringExtra("playerId") ?: "1"
        Log.d("PlayerStatsActivity", "Erhaltener Spieler-ID: $playerId")

        setContent {
            MaterialTheme {
                StatsScreenWithCloseButton(playerId)
            }
        }
    }

    @Composable
    fun StatsScreenWithCloseButton(playerId: String) {
        var player by remember { mutableStateOf(PlayerManager.getPlayer(playerId)) }

        // 🔄 1x zu Beginn vom Server laden
        LaunchedEffect(playerId) {
            PlayerManager.syncPlayerWithServer(playerId)
            player = PlayerManager.getPlayer(playerId)
        }

        // 🔁 Wiederholt synchronisieren (alle 2 Sekunden)
        LaunchedEffect(playerId) {
            while (true) {
                PlayerManager.syncPlayerWithServer(playerId)
                player = PlayerManager.getPlayer(playerId)
                delay(2000)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (player != null) {
                val playerModel = PlayerModell(
                    id = player!!.id,
                    money = player!!.money,
                    children = player!!.children,
                    education = player!!.hasEducation,
                    investments = player!!.investments,
                    salary = player!!.salary,
                    relationship = player!!.relationship
                )
                PlayerStatsOverlay(player = playerModel)
            } else {
                Text("⚠️ Spieler nicht gefunden", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { finish() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("🔙 Zurück zum Spiel")
            }
        }
    }
}
