package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.viewModels.ConnectionViewModel
import at.aau.serg.sdlapp.network.viewModels.getSharedViewModel
import at.aau.serg.sdlapp.ui.activity.HomeScreenActivity
import kotlinx.coroutines.launch

class StartActivity : ComponentActivity() {
    private lateinit var stomp: StompConnectionManager
    private val viewModel: ConnectionViewModel by lazy { getSharedViewModel() }
    private var isConnecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initializeStomp { message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        stomp = viewModel.myStomp.value!!

        enableFullscreen()

        setContent {
            StartScreen(
                onOpenSettings = {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StartScreen(
        onOpenSettings: () -> Unit
    ) {
        var playerName by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }
        var showErrorRegistration by remember { mutableStateOf(false) }
        val textColor = Color.White
        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background_mainscreen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Das Spiel des Lebens",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .padding(top = 35.dp)
                        .padding(bottom = 35.dp)
                        .align(Alignment.CenterHorizontally)
                )

                TextField(
                    value = playerName,
                    onValueChange = {
                        playerName = it
                        showError = false
                        showErrorRegistration = false
                    },
                    placeholder = {
                        Text(
                            text = "Name eingeben",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    isError = showError || showErrorRegistration,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(bottom = 8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                if (showError) {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(0.4f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "Bitte Namen eingeben",
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (showErrorRegistration) {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(0.4f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "Name bereits vergeben",
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        //überprüfen, ob Spieler einen Namen eingegeben hat
                        if (playerName.trim().isNotEmpty()) {
                            playerName = playerName.trim()
                            isConnecting = true
                            scope.launch {
                                val deferred = stomp.connectAsync(playerName)
                                val success = deferred.await()
                                Log.d("Debugging", "success: $success")
                                if (success) {
                                    showErrorRegistration = false
                                    val intent = Intent(this@StartActivity, HomeScreenActivity::class.java)
                                    intent.putExtra("playerName", playerName)
                                    startActivity(intent)
                                } else {
                                    showErrorRegistration = true
                                    isConnecting = false
                                }
                            }
                        } else {
                            showError = true
                        }
                    },
                    enabled = !isConnecting,
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Spiel starten", fontSize = 20.sp)
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }
    }

    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}



