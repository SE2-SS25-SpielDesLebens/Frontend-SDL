package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        enableFullscreen()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val startBtn = findViewById<Button>(R.id.startGameBtn)
        val settingsBtn = findViewById<ImageButton>(R.id.settingsBtn)

        // Spiel starten → NEU: Öffnet BoardActivity
        startBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val intent = Intent(this, HomeScreenActivity::class.java)

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
    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
