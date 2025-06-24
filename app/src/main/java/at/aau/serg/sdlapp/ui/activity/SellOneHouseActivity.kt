package at.aau.serg.sdlapp.ui.activity

import android.app.Activity
import android.content.Intent
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

class SellOneHouseActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1
    private lateinit var house: HouseMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_onehouse)

        // Parameter aus Intent
        playerName = intent.getStringExtra("playerName") ?: error("playerName fehlt")
        gameId = intent.getIntExtra("gameId", -1)
        val housesJson = intent.getStringExtra("housesJson") ?: error("housesJson fehlt")

        // Ein Haus auswählen
        val type = object : TypeToken<List<HouseMessage>>() {}.type
        val houses: List<HouseMessage> = Gson().fromJson(housesJson, type)
        if (houses.isEmpty()) {
            showToast("Keine Häuser zum Verkaufen vorhanden")
            finish()
            return
        }
        house = houses.first()

        // STOMP-Verbindung initialisieren
        stomp = StompConnectionManager( { msg -> showToast(msg) })
        stomp.connectAsync(playerName) { connected ->
            if (!connected) showToast("Verbindung fehlgeschlagen")
        }

        // Daten anzeigen
        findViewById<TextView>(R.id.tvDescription).text = house.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePrice).text = "Kaufpreis: ${house.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceRed).text = "Verkauf: ${house.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceBlack).text = "Verkauf: ${house.verkaufspreisSchwarz}"

        // Verkaufs-Button
        findViewById<Button>(R.id.btnBuy).setOnClickListener {
            val randomNumber = (1..10).random()
            val isEven = randomNumber % 2 == 0

            // Neues HouseMessage mit gesetztem sellPrice (true bei gerade)
            val modifiedHouse = house.copy(sellPrice = isEven)

            // Finalisierungsanfrage sofort senden
            stomp.finalizeHouseAction(gameId, playerName, modifiedHouse)

            // Jetzt WheelActivity anzeigen → als reine Info → KEIN finish!
            val intent = Intent(this, WheelActivity::class.java)
            intent.putExtra("dice", randomNumber)
            startActivity(intent)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
