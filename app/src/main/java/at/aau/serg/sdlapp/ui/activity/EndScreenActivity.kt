package at.aau.serg.sdlapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.viewModels.GameResultViewModel

class EndScreenActivity : ComponentActivity() {

    private val viewModel: GameResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EndScreen(viewModel)
            }
        }
    }
}

@Composable
fun EndScreen(viewModel: GameResultViewModel) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏁 Spiel beendet", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            Text("🏆 Leaderboard", fontSize = 24.sp, color = Color.White)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
            ) {
                itemsIndexed(viewModel.sortedPlayers) { index, player ->
                    Text(
                        "${index + 1}. ${player.name}: ${player.money + player.investments} €",
                        color = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("📊 Kategorien", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            CategoryItem("👶 Meiste Kinder", viewModel.mostChildren)
            CategoryItem("📈 Top-Investor", viewModel.topInvestor)
            CategoryItem("💼 Höchstes Gehalt", viewModel.highestSalary)
            CategoryItem("🎓 Akademiker:innen", viewModel.academics)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.clearGameData()
                    val intent = Intent(context, StartActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                },
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(0.8f)
            ) {
                Text("🔙 Zurück zum Hauptmenü")
            }
        }
    }
}

@Composable
fun CategoryItem(label: String, winner: String) {
    Text(
        "• $label: $winner",
        fontSize = 16.sp,
        color = Color.White,
        modifier = Modifier.padding(4.dp)
    )
}


