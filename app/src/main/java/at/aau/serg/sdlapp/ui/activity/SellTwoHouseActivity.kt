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

class SellTwoHouseActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1
    private lateinit var leftHouse: HouseMessage
    private lateinit var rightHouse: HouseMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_twohouse)

        // Parameter aus Intent
        playerName = intent.getStringExtra("playerName") ?: error("playerName fehlt")
        gameId = intent.getIntExtra("gameId", -1)
        val housesJson = intent.getStringExtra("housesJson") ?: error("housesJson fehlt")

        // Zwei Häuser auswählen
        val type = object : TypeToken<List<HouseMessage>>() {}.type
        val houses: List<HouseMessage> = Gson().fromJson(housesJson, type)
        if (houses.size < 2) {
            showToast("Nicht genügend Häuser zum Verkaufen vorhanden")
            finish()
            return
        }
        leftHouse = houses[0]
        rightHouse = houses[1]

        // STOMP-Verbindung initialisieren
        stomp = StompConnectionManager ({ msg -> showToast(msg) })
        stomp.connectAsync(playerName)

        // Linkes Haus anzeigen
        findViewById<TextView>(R.id.tvDescriptionLeft).text = leftHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceLeft).text = "Kaufpreis: ${leftHouse.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceLeft).text = "Verkauf: ${leftHouse.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceLeftSecond).text = "Verkauf: ${leftHouse.verkaufspreisSchwarz}"

        // Rechtes Haus anzeigen
        findViewById<TextView>(R.id.tvDescriptionRight).text = rightHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceRight).text = "Kaufpreis: ${rightHouse.kaufpreis}"
        findViewById<TextView>(R.id.tvSalePriceRight).text = "Verkauf: ${rightHouse.verkaufspreisRot}"
        findViewById<TextView>(R.id.tvSalePriceRightSecond).text = "Verkauf: ${rightHouse.verkaufspreisSchwarz}"

        // Linker Verkaufs-Button
        findViewById<Button>(R.id.btnLeftBuy).setOnClickListener {
            val randomNumber = (1..10).random()
            val isEven = randomNumber % 2 == 0

            val modifiedHouse = leftHouse.copy(sellPrice = isEven)

            // Finalisierungsanfrage senden
            stomp.finalizeHouseAction(gameId, playerName, modifiedHouse)

            // Rad anzeigen
            val intent = Intent(this, WheelActivity::class.java)
            intent.putExtra("dice", randomNumber)
            startActivity(intent)

            // Result zurück an HouseCardFunctionalityActivity
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Rechter Verkaufs-Button
        findViewById<Button>(R.id.btnRightBuy).setOnClickListener {
            val randomNumber = (1..10).random()
            val isEven = randomNumber % 2 == 0

            val modifiedHouse = rightHouse.copy(sellPrice = isEven)

            // Finalisierungsanfrage senden
            stomp.finalizeHouseAction(gameId, playerName, modifiedHouse)

            // Rad anzeigen
            val intent = Intent(this, WheelActivity::class.java)
            intent.putExtra("dice", randomNumber)
            startActivity(intent)

            // Result zurück an HouseCardFunctionalityActivity
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
