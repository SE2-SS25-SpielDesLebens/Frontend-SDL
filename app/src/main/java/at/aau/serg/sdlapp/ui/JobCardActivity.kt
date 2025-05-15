package at.aau.serg.sdlapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson

class JobCardActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private val gameId: Int = 1
    private val hasDegree = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = MyStomp { showToast(it) }

        val btnConnect       = findViewById<Button>(R.id.btnConnect)
        val btnSendGameStart = findViewById<Button>(R.id.btnCreateRepo)
        val btnRequestJobs   = findViewById<Button>(R.id.btnRequestJobs)

        btnConnect.setOnClickListener {
            stomp.connectAsync(playerName)
            showToast("Verbindung gestartet")
        }

        btnSendGameStart.setOnClickListener {
            stomp.sendGameStart(gameId, playerName)
            showToast("Spielstart gesendet (Repo wird erstellt)")
        }

        btnRequestJobs.setOnClickListener {
            stomp.subscribeJobs(gameId, playerName) { jobs ->
                val jobsJson = Gson().toJson(jobs)
                Intent(this, JobSelectionActivity::class.java).apply {
                    putExtra("gameId", gameId)
                    putExtra("playerName", playerName)
                    putExtra("hasDegree", hasDegree)
                    putExtra("jobList", jobsJson)
                    startActivity(this)
                }
            }
            stomp.requestJobs(gameId, playerName, hasDegree)
        }
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
