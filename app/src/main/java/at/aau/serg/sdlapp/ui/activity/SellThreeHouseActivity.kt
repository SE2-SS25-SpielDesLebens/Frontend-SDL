package at.aau.serg.sdlapp.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.house.HouseMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SellThreeHouseActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1

    private lateinit var centerHouse: HouseMessage
    private lateinit var bottomLeftHouse: HouseMessage
    private lateinit var bottomRightHouse: HouseMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_threehouse)

        // Intent-Parameter auslesen
        playerName = intent.getStringExtra("playerName") ?: error("playerName fehlt")
        gameId = intent.getIntExtra("gameId", -1)
        val housesJson = intent.getStringExtra("housesJson") ?: error("housesJson fehlt")

        // Drei Häuser auswählen
        val type = object : TypeToken<List<HouseMessage>>() {}.type
        val houses: List<HouseMessage> = Gson().fromJson(housesJson, type)
        if (houses.size < 3) {
            showToast("Nicht genügend Häuser zum Verkaufen vorhanden")
            finish()
            return
        }
        centerHouse     = houses[0]
        bottomLeftHouse = houses[1]
        bottomRightHouse= houses[2]

        // STOMP-Verbindung initialisieren
        stomp = StompConnectionManager { msg -> showToast(msg) }
        stomp.connectAsync(playerName) { connected ->
            if (!connected) showToast("Verbindung fehlgeschlagen")
        }

        // --- Center House UI ---
        findViewById<TextView>(R.id.tvDescriptionCenter).text           = centerHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceCenter).text         = "Kaufpreis: ${centerHouse.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceCenterRed).text          = "Verkauf: ${centerHouse.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceCenterBlack).text        = "Verkauf: ${centerHouse.verkaufspreisSchwarz}"
        findViewById<Button>(R.id.btnCenterSell).setOnClickListener {
            stomp.finalizeHouseAction(gameId, playerName, centerHouse)
            showToast("Zentrales Hausverkauf gesendet")
            finish()
        }

        // --- Bottom Left House UI ---
        findViewById<TextView>(R.id.tvDescriptionBottomLeft).text       = bottomLeftHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceBottomLeft).text     = "Kaufpreis: ${bottomLeftHouse.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceBottomLeftRed).text      = "Verkauf: ${bottomLeftHouse.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceBottomLeftBlack).text    = "Verkauf: ${bottomLeftHouse.verkaufspreisSchwarz}"
        findViewById<Button>(R.id.btnBottomLeftSell).setOnClickListener {
            stomp.finalizeHouseAction(gameId, playerName, bottomLeftHouse)
            showToast("Linkes unteres Hausverkauf gesendet")
            finish()
        }

        // --- Bottom Right House UI ---
        findViewById<TextView>(R.id.tvDescriptionBottomRight).text      = bottomRightHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceBottomRight).text    = "Kaufpreis: ${bottomRightHouse.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceBottomRightRed).text     = "Verkauf: ${bottomRightHouse.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceBottomRightBlack).text   = "Verkauf: ${bottomRightHouse.verkaufspreisSchwarz}"
        findViewById<Button>(R.id.btnBottomRightSell).setOnClickListener {
            stomp.finalizeHouseAction(gameId, playerName, bottomRightHouse)
            showToast("Rechtes unteres Hausverkauf gesendet")
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
