package at.aau.serg.sdlapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val startBtn = findViewById<Button>(R.id.startGameBtn)
        val settingsBtn = findViewById<ImageButton>(R.id.settingsBtn)

        // Spiel starten
        startBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("playerName", name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show()
            }
        }


        // Einstellungen öffnen
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
