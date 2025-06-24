package at.aau.serg.sdlapp.ui.activity

import android.app.Activity
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

class BuyHouseActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private lateinit var leftHouse: HouseMessage
    private lateinit var rightHouse: HouseMessage
    private var gameId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_house)

        // 1) Parameter aus Intent
        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        gameId     = intent.getIntExtra("gameId", -1)

        // 2) STOMP-Client erzeugen und verbinden
        stomp = StompConnectionManager( { showToast(it) })
        stomp.connectAsync(playerName)

        // 3) House-Liste aus Intent parsen
        val housesJson = intent.getStringExtra("houseList") ?: "[]"
        val type       = object : TypeToken<List<HouseMessage>>() {}.type
        val houses: List<HouseMessage> = Gson().fromJson(housesJson, type)
        leftHouse  = houses.getOrNull(0) ?: return finish()
        rightHouse = houses.getOrNull(1) ?: return finish()

        // 4) UI befüllen
        findViewById<TextView>(R.id.tvDescriptionLeft).text       = leftHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceLeft).text     = "${leftHouse.kaufpreis} €"
        findViewById<TextView>(R.id.tvSalePriceLeft).text         = "${leftHouse.verkaufspreisRot} €"
        findViewById<TextView>(R.id.tvSalePriceLeftSecond).text   = "${leftHouse.verkaufspreisSchwarz} €"
        findViewById<Button>(R.id.btnLeftBuy).apply {
            isEnabled = !leftHouse.isTaken
            setOnClickListener {
                showToast("Haus ausgewählt: ${leftHouse.bezeichnung}")
                stomp.finalizeHouseAction(gameId, playerName, leftHouse)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        findViewById<TextView>(R.id.tvDescriptionRight).text      = rightHouse.bezeichnung
        findViewById<TextView>(R.id.tvPurchasePriceRight).text    = "${rightHouse.kaufpreis} €"
        findViewById<TextView>(R.id.tvSalePriceRight).text        = "${rightHouse.verkaufspreisRot} €"
        findViewById<TextView>(R.id.tvSalePriceRightSecond).text  = "${rightHouse.verkaufspreisSchwarz} €"
        findViewById<Button>(R.id.btnRightBuy).apply {
            isEnabled = !rightHouse.isTaken
            setOnClickListener {
                showToast("Haus ausgewählt: ${rightHouse.bezeichnung}")
                stomp.finalizeHouseAction(gameId, playerName, rightHouse)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
