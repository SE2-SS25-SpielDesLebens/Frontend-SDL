package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager

class HouseCardActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private val gameId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = StompConnectionManager( { showToast(it) })

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            stomp.connectAsync(playerName)
        }

        findViewById<Button>(R.id.btnCreateHouseRepo).setOnClickListener {
            stomp.requestHouseRepository(gameId)
        }

        findViewById<Button>(R.id.btnHouseFunctionality).setOnClickListener {
            // Navigation zur HouseCardFunctionalityActivity
            val intent = Intent(this, HouseCardFunctionalityActivity::class.java).apply {
                putExtra("playerName", playerName)
                putExtra("gameId", gameId)
            }
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
