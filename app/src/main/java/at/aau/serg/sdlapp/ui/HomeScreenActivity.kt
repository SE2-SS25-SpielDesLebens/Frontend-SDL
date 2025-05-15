package at.aau.serg.sdlapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.network.MyStomp
import at.aau.serg.sdlapp.network.viewModels.ConnectionViewModel
import at.aau.serg.sdlapp.network.viewModels.getSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeScreenActivity : ComponentActivity() {
    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private val scope = CoroutineScope(Dispatchers.IO)
    private val viewModel: ConnectionViewModel by lazy { getSharedViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //gets playername, maybe change to playername + boolean isHost later
        playerName = intent.getStringExtra("playerName") ?: "Spieler"

        viewModel.initializeStomp { message ->
            Log.d("Debugging", "got here")
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        stomp = viewModel.myStomp.value!!
        setContent {
            HomeScreen()
        }
    }

    @Composable
    fun HomeScreen() {
        val textColor = Color.White
        var showTextField by remember { mutableStateOf(false) }
        var lobbyId by remember { mutableStateOf("") }
        val context = LocalContext.current

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Das Spiel des Lebens",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier
                    .padding(top = 35.dp)
                    .padding(bottom = 25.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = {
                    stomp.connectAsync(playerName)
                    showToast("Verbindung gestartet")
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)

            ) {
                Text("Verbinden")
            }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        val newLobbyId = withContext(Dispatchers.IO) {
                            stomp.sendLobbyCreate(playerName)
                        }
                        // Direkt danach neue Activity starten
                        val intent = Intent(this@HomeScreenActivity, LobbyActivity::class.java)
                        intent.putExtra("lobbyID", newLobbyId)
                        intent.putExtra("player", playerName)
                        startActivity(intent)
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Lobby erstellen")
            }
            Button(
                onClick = {
                    showTextField = true
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Lobby beitreten")
            }
            if (showTextField) {
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(showTextField) {
                    delay(100) //bis Textfield im Layout ist
                    focusRequester.requestFocus()
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TextField(
                        value = lobbyId,
                        onValueChange = { lobbyId = it },
                        placeholder = {
                            Text(
                                text = "Lobby-ID eingeben",
                                color = Color.Gray
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(focusRequester = focusRequester)
                            .align(Alignment.CenterHorizontally),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (lobbyId.trim().isBlank()) return@KeyboardActions
                                scope.launch {
                                    val response = stomp.sendLobbyJoin(playerName, lobbyId)
                                    if (response?.isSuccessful == true) {
                                        Log.d("Debugging", "$lobbyId $playerName")
                                        val intent = Intent(
                                            this@HomeScreenActivity,
                                            LobbyActivity::class.java
                                        ).apply {
                                            putExtra("lobbyID", lobbyId)
                                            putExtra("player", playerName)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        showToast(
                                            response?.message ?: "Beitritt fehlgeschlagen"
                                        )
                                    }

                                }
                            }
                        )

                    )
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {

                Button(
                    onClick = {
                        val intent = Intent(this@HomeScreenActivity, JobCardActivity::class.java)
                        intent.putExtra("player", playerName)
                        startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text("Job Activity")
                }

                Button(
                    onClick = {
                        val dice = (1..10).random()
                        val intent = Intent(this@HomeScreenActivity, WheelActivity::class.java)
                        intent.putExtra("dice", dice)
                        startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                ){
                    Text("Am Rad drehen")
                }
            }

        }
    }

    //function to switch to lobby screen, using lobby id as parameter
    @Composable
    fun StartLobbyScreen(lobbyid: String?) {
        val context = LocalContext.current
        Intent(context, LobbyActivity::class.java).apply {
            intent.putExtra("Lobby-ID", lobbyid)
            startActivity(intent)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @Preview
    @Composable
    fun HomePreview() {
        HomeScreen()
    }
}

