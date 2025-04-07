package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.network.MyStomp
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity(), Callbacks {

    lateinit var stomp: MyStomp


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_fullscreen)

        val playerName = intent.getStringExtra("playerName") ?: "Spieler"

        stomp = MyStomp(this)

        findViewById<Button>(R.id.connectbtn).setOnClickListener {
            stomp.connect()
        }

        findViewById<Button>(R.id.hellobtn).setOnClickListener {
            stomp.sendMove(playerName, "würfelt 6")
        }

        findViewById<Button>(R.id.jsonbtn).setOnClickListener {
            stomp.sendChat(playerName, "Hallo an alle!")
        }

        findViewById<Button>(R.id.jobRequestBtn).setOnClickListener {
            stomp.sendJobRequest(playerName)
        }

        findViewById<Button>(R.id.jobReleaseBtn).setOnClickListener {
            stomp.sendJobRelease(playerName)
        }
    }

    override fun onResponse(res: String) {
        runOnUiThread {
            when {
                res.contains("Verbunden") -> {
                    updateStatus("🟢 $res", R.color.status_connected)
                }
                res.contains("Nicht verbunden") || res.contains("Getrennt") -> {
                    updateStatus("🔴 $res", R.color.status_disconnected)
                }
                res.contains("Fehler") -> {
                    updateStatus("🟠 $res", R.color.status_error)
                    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    updateStatus("ℹ️ $res", R.color.black)
                }
            }
        }
    }

    private fun updateStatus(text: String, colorResId: Int) {
        val status = findViewById<TextView>(R.id.statusText)
        status.text = text
        status.setTextColor(resources.getColor(colorResId, theme))
    }
}

