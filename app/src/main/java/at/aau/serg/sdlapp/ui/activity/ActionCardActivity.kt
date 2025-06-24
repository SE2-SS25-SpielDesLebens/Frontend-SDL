package at.aau.serg.sdlapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.game.ActionCard
import at.aau.serg.sdlapp.network.StompConnectionManager
import kotlinx.coroutines.Dispatchers

class ActionCardActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerId: String
    private lateinit var lobbyId: String
    private lateinit var currentCard: ActionCard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_card)

        //TODO: Get playerId and lobbyId from proper source
        playerId = intent.getStringExtra("playerId") ?: "Spieler"
        lobbyId = intent.getStringExtra("lobbyId") ?: "defaultLobby"

        stomp = StompConnectionManager(
            callback = { message: String -> Log.d("ActionCard", message) },
            ioDispatcher = Dispatchers.IO,
            mainDispatcher = Dispatchers.Main
        )

        stomp.connectAsync(playerId) { connected ->
            if (connected) {
                stomp.subscribeToActionCard(playerId, lobbyId)
                stomp.drawActionCard(playerId, lobbyId)
            }
        }

        stomp.onActionCardReceived = { card ->
            currentCard = card
            handleResponse(card)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun handleResponse(card: ActionCard) {
        runOnUiThread {
            findViewById<TextView>(R.id.headline).text = card.headline
            val resId = resources.getIdentifier(card.imageName, "drawable", applicationContext.packageName)
            if (resId != 0) findViewById<ImageView>(R.id.picture).setImageResource(resId)
            findViewById<TextView>(R.id.description).text = card.action

            val button1 = findViewById<Button>(R.id.button)
            val button2 = findViewById<Button>(R.id.button2)

            button1.text = card.choices.getOrNull(0) ?: "OK"
            button1.setOnClickListener {
                buttonClicked(button1, card.choices[0])
            }

            if (card.choices.size > 1) {
                button2.text = card.choices[1]
                button2.setOnClickListener {
                    buttonClicked(button2, card.choices[1])
                }
            } else {
                (button2.parent as? ViewGroup)?.removeView(button2)

                // Move the first button (R.id.button) slightly downward
                val button = findViewById<Button>(R.id.button)
                val params = button.layoutParams as? ViewGroup.MarginLayoutParams
                params?.topMargin = dpToPx(55)
                button.layoutParams = params
            }
        }
    }

    private fun buttonClicked(button: Button, choice: String) {
        button.isEnabled = false
        stomp.playActionCard(playerId, lobbyId, choice)
        finish()
        //TODO: go to next screen
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}
