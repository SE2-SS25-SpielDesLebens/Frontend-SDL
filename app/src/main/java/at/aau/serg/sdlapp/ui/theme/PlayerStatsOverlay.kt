package at.aau.serg.sdlapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.ui.theme.PlayerModell

@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Abstand zwischen den Karten
    ) {
        Text(text = "Spieler ${player.id} Statistiken", fontSize = 30.sp, color = Color.Blue, modifier = Modifier.align(Alignment.CenterHorizontally))

        // Jede Statistik in einer eigenen Karte
        StatCard("Geld", "${player.money}$", getMoneyColor(player.money))
        StatCard("Gehalt", "${player.salary}$", getSalaryColor(player.salary))
        StatCard("Beruf", player.career, Color.White)
        StatCard("Bildung", player.education, Color.Green)
        StatCard("Beziehung", player.relationship, Color.Blue)
        StatCard("Investitionen", player.investments.toString(), Color.Yellow)
        StatCard("Kinder", player.children.toString(), Color.Black)
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 18.sp, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, color = color)
        }
    }
}


@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = color)
        Text(text = value, fontSize = 16.sp, color = color)
    }
}

// Funktion zur Bestimmung der Farbe für Geld basierend auf dem Wert
fun getMoneyColor(money: Int): Color {
    return when {
        money > 10000 -> Color.Green   // Viel Geld -> grün
        money > 5000  -> Color.Yellow  // Mittelwert -> gelb
        else           -> Color.Red     // Wenig Geld -> rot
    }
}

// Funktion zur Bestimmung der Farbe für Gehalt basierend auf dem Wert
fun getSalaryColor(salary: Int): Color {
    return when {
        salary > 5000 -> Color.Green   // Hoher Gehalt -> grün
        salary > 2500 -> Color.Yellow  // Mittlerer Gehalt -> gelb
        else          -> Color.Red     // Niedriges Gehalt -> rot
    }
}
