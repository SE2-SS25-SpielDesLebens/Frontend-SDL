package at.aau.serg.sdlapp.ui.PlayerOverlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.model.player.PlayerModell

@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8D4C28)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Spielername & Geld
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.id,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "${player.money / 1000}k €",
                    color = Color.White,
                    fontSize = 22.sp
                )

            }

            // Statussymbole
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                StatusIcon("❤️", player.children.toString())
                StatusIcon("📘", if (player.education) "✓" else "✗")
                StatusIcon("💰", "${player.investments / 1000}k")
            }
        }
    }
}

@Composable
fun StatusIcon(emoji: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = emoji, fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, fontSize = 14.sp, color = Color.White)
    }
}

