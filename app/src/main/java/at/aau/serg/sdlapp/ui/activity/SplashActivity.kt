package at.aau.serg.sdlapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import at.aau.serg.sdlapp.R

class SplashActivity : ComponentActivity() {

    private val SPLASH_DELAY = 2000L // 2 Sekunden

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Vollbild-Einstellungen vor setContentView anwenden
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        enableEdgeToEdge()
        
        // Layout mit Logo laden
        setContentView(R.layout.activity_splash)
        
        // Logo-Größe maximieren
        val logoImageView = findViewById<ImageView>(R.id.splashLogo)
        logoImageView.adjustViewBounds = true
        logoImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        
        // Logo maximieren (80% der Bildschirmbreite)
        ViewCompat.setOnApplyWindowInsetsListener(logoImageView) { view, windowInsets ->
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            val logoSize = (Math.min(screenWidth, screenHeight) * 0.8).toInt()
            
            view.updateLayoutParams<ViewGroup.LayoutParams> {
                width = logoSize
                height = logoSize
            }
            windowInsets
        }

        // Nach Verzögerung zur StartActivity wechseln
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, StartActivity::class.java)
            startActivity(intent)
            finish() // SplashActivity beenden
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY)
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
