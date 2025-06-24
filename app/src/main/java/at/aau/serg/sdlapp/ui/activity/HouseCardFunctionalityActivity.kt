package at.aau.serg.sdlapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.house.HouseMessage
import com.google.gson.Gson

class HouseCardFunctionalityActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1

    companion object {
        private const val REQUEST_CODE_SELL = 1001
        private const val REQUEST_CODE_BUY  = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_functionality)

        // Parameter aus Intent
        playerName = intent.getStringExtra("playerName")
            ?: error("playerName fehlt")
        gameId = intent.getIntExtra("gameId", gameId)

        // STOMP-Verbindung initialisieren
        stomp = StompConnectionManager ({ msg -> showToast(msg) })
        stomp.connectAsync(playerName) { connected ->
            if (!connected) showToast("Verbindung fehlgeschlagen")
        }

        // Kauf-Button: erst abonnieren, dann senden, dann BuyHouseActivity starten
        findViewById<Button>(R.id.btnBuyHouse).setOnClickListener {
            subscribeAndShowBuyScreen()
        }

        // Verkaufs-Button: erst abonnieren, dann senden, dann in SellHouseActivity wechseln
        findViewById<Button>(R.id.btnSellHouse).setOnClickListener {
            subscribeAndShowSellScreen()
        }

        // „Nichts machen“-Button
        findViewById<Button>(R.id.btnDoNothing).setOnClickListener {
            finish()
        }
    }

    /**
     * Buy-Flow: Erst abonnieren, dann Kauf-Anfrage senden
     * und in die BuyHouseActivity wechseln.
     */
    private fun subscribeAndShowBuyScreen() {
        stomp.subscribeHouses(gameId, playerName) { houses ->
            runOnUiThread {
                if (houses.isEmpty()) {
                    showToast("Keine Häuser verfügbar")
                    return@runOnUiThread
                }
                // Starte BuyHouseActivity → JETZT mit startActivityForResult
                val intent = Intent(this, BuyHouseActivity::class.java).apply {
                    putExtra("playerName", playerName)
                    putExtra("gameId", gameId)
                    putExtra("houseList", Gson().toJson(houses))
                }
                startActivityForResult(intent, REQUEST_CODE_BUY)  // NEU
            }
        }
        stomp.buyHouse(gameId, playerName)
    }


    /**
     * Sell-Flow: Erst abonnieren, dann Verkaufs-Anfrage senden
     * und in den passenden SellScreen wechseln.
     */
    private fun subscribeAndShowSellScreen() {
        stomp.subscribeHouses(gameId, playerName) { houses ->
            runOnUiThread {
                when (houses.size) {
                    0 -> showToast("Kein Haus zum Verkaufen")
                    1 -> launchSellScreen("activity_sell_onehouse", houses)
                    2 -> launchSellScreen("activity_sell_twohouse", houses)
                    3 -> launchSellScreen("activity_sell_threehouse", houses)
                    else -> showToast("Unerwartete Anzahl an Häusern: ${houses.size}")
                }
            }
        }
        stomp.sellHouse(gameId, playerName)
    }

    /**
     * Generische Navigation zu den Sell-Screens.
     * --> jetzt mit startActivityForResult, damit wir später finish() machen können.
     */
    private fun launchSellScreen(layoutName: String, houses: List<HouseMessage>) {
        val target = when (layoutName) {
            "activity_sell_onehouse"   -> SellOneHouseActivity::class.java
            "activity_sell_twohouse"   -> SellTwoHouseActivity::class.java
            "activity_sell_threehouse" -> SellThreeHouseActivity::class.java
            else                        -> null
        }
        target?.let {
            val intent = Intent(this, it).apply {
                putExtra("playerName", playerName)
                putExtra("gameId", gameId)
                putExtra("housesJson", Gson().toJson(houses))
            }
            startActivityForResult(intent, REQUEST_CODE_SELL)
        } ?: showToast("Screen nicht gefunden: $layoutName")
    }

    /**
     * Ergebnisbehandlung für Sell-Screens.
     * Wenn sie mit RESULT_OK zurückkommen → diese Activity beenden.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == REQUEST_CODE_SELL || requestCode == REQUEST_CODE_BUY) && resultCode == Activity.RESULT_OK) {
            // Sell- oder Buy-Screen fertig → wir beenden uns selbst
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
