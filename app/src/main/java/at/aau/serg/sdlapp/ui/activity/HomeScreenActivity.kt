package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.viewModels.ConnectionViewModel
import at.aau.serg.sdlapp.network.viewModels.getSharedViewModel
import kotlinx.coroutines.*

class HomeScreenActivity : ComponentActivity() {
    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private val scope = CoroutineScope(Dispatchers.IO)
    private val viewModel: ConnectionViewModel by lazy { getSharedViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerName = intent.getStringExtra("playerName") ?: "Spieler"

        // Initialisiere Stomp nur, wenn es noch nicht existiert
        if (viewModel.myStomp.value == null) {
            viewModel.initializeStomp { message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
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
        var showWarning by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

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
                    CoroutineScope(Dispatchers.Main).launch {
                        val newLobbyId = withContext(Dispatchers.IO) {
                            stomp.sendLobbyCreate(playerName)
                        }
                        delay(3000)
                        val intent = Intent(this@HomeScreenActivity, LobbyActivity::class.java)
                        intent.putExtra("lobbyID", newLobbyId)
                        intent.putExtra("player", playerName)
                        startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("Lobby erstellen", fontSize = 20.sp)
            }

            Button(
                onClick = {
                    showTextField = true
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("Lobby beitreten", fontSize = 20.sp)
            }

            Button(
                onClick = {
                    // Nur verbinden wenn noch keine Verbindung besteht
                    if (!stomp.isConnected) {
                        stomp.connectAsync(playerName)
                        showToast("Verbindung gestartet")
                    } else {
                        showToast("Bereits verbunden")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text(
                    text = if (!stomp.isConnected) "Verbinden" else "Verbunden",
                    fontSize = 20.sp
                )
            }

            if (showTextField) {
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
                        onValueChange = {
                            lobbyId = it
                            showWarning = false // Fehlermeldung zurücksetzen, wenn Nutzer tippt
                        },
                        modifier = Modifier.focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = "Lobby-ID eingeben",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                                        Log.e("Lobby-Beitritt", response?.message ?: "Beitritt fehlgeschlagen")
                                        withContext(Dispatchers.Main) { showWarning = true }
                                    }
                                }
                            }
                        )
                    )
                    if (showWarning) {
                        LaunchedEffect(showWarning) {
                            focusManager.clearFocus()
                        }
                        Text(
                            text = "Lobby-ID existiert nicht",
                            fontSize = 15.sp,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


}

