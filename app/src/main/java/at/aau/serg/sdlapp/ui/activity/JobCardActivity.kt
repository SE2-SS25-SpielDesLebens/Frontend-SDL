package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import com.google.gson.Gson

class JobCardActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        gameId = intent.getIntExtra("gameId", gameId)
        stomp = StompConnectionManager ({ showToast(it) })

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            stomp.connectAsync(playerName)
        }

        findViewById<Button>(R.id.btnCreateRepo).setOnClickListener {
            stomp.requestJobRepository(gameId)
        }

        findViewById<Button>(R.id.btnRequestJobs).setOnClickListener {
            // 1) Subscription auf Job-Topic
            stomp.subscribeJobs(gameId, playerName) { jobs ->
                // 2) Job-Liste ist da → navigiere weiter
                val jobsJson = Gson().toJson(jobs)
                val intent = Intent(this, JobSelectionActivity::class.java).apply {
                    putExtra("gameId", gameId)
                    putExtra("playerName", playerName)
                    putExtra("jobList", jobsJson)
                }
                startActivity(intent)
            }
            // 3) Anfrage ans Backend schicken
            stomp.requestJobs(gameId, playerName)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
