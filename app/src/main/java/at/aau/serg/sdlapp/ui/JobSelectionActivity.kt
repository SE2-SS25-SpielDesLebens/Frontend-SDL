package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.messaging.JobMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Can be deleted later, just for Functionality Test
class JobSelectionActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private lateinit var leftJob: JobMessage
    private lateinit var rightJob: JobMessage
    private var gameId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_selection)

        // 1) Parameter aus Intent
        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        gameId     = intent.getIntExtra("gameId", 1)

        // 2) STOMP-Client erzeugen und verbinden
        stomp = MyStomp { showToast(it) }
        stomp.connectAsync(playerName)

        // 3) Job-Liste aus Intent parsen
        val jobsJson = intent.getStringExtra("jobList") ?: "[]"
        val type     = object : TypeToken<List<JobMessage>>() {}.type
        val jobs: List<JobMessage> = Gson().fromJson(jobsJson, type)
        leftJob  = jobs[0]
        rightJob = jobs[1]

        // 4) UI befüllen
        findViewById<TextView>(R.id.tvTitleLeft).text   = leftJob.title
        findViewById<TextView>(R.id.tvSalaryLeft).text  = "${leftJob.salary} €"
        findViewById<TextView>(R.id.tvBonusLeft).text   = "Bonus: ${leftJob.bonusSalary} €"

        findViewById<TextView>(R.id.tvTitleRight).text  = rightJob.title
        findViewById<TextView>(R.id.tvSalaryRight).text = "${rightJob.salary} €"
        findViewById<TextView>(R.id.tvBonusRight).text  = "Bonus: ${rightJob.bonusSalary} €"

        // 5) Buttons: Job annehmen
        findViewById<Button>(R.id.btnLeftAccept).setOnClickListener {
            showToast("Job angenommen: ${leftJob.title}")
            stomp.selectJob(gameId, playerName, leftJob)
            finish()
        }
        findViewById<Button>(R.id.btnRightAccept).setOnClickListener {
            showToast("Job angenommen: ${rightJob.title}")
            stomp.selectJob(gameId, playerName, rightJob)
            finish()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
