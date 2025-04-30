package at.aau.serg.sdlapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity


class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Beispiel-Aktion
        Toast.makeText(this, "Einstellungen geladen", Toast.LENGTH_SHORT).show()
    }
}