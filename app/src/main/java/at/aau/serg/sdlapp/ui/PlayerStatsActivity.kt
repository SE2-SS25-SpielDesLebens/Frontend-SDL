package at.aau.serg.sdlapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.ui.theme.PlayerStatsOverlayScreen

class PlayerStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerId = intent.getIntExtra("playerId", 1)

        setContent {
            MaterialTheme {
                StatsScreenWithCloseButton(playerId = playerId) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}

@Composable
fun StatsScreenWithCloseButton(playerId: Int, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlayerStatsOverlayScreen(playerId = playerId)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        ) {
            Text("🔙 Zurück zum Spiel")
        }
    }
}
