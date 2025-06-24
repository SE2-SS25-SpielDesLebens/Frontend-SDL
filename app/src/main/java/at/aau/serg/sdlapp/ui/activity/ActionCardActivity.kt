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
import com.google.gson.Gson


class ActionCardActivity : ComponentActivity(){

    private lateinit var stomp: StompConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_card)

        val playerName = intent.getStringExtra("playerName") ?: "Spieler"

        stomp = StompConnectionManager({message -> Log.d("ActionCard", message)})
        stomp.sendMove(playerName, "zieht Action Card")
    }

    @SuppressLint("DiscouragedApi")
    private fun handleResponse(res: String) {
        runOnUiThread {
            val gson = Gson()
            val card = gson.fromJson(res, ActionCard::class.java)

            findViewById<TextView>(R.id.headline).text = card.headline

            val resId = resources.getIdentifier(card.imageName, "drawable", applicationContext.packageName)
            if (resId != 0) findViewById<ImageView>(R.id.picture).setImageResource(resId)

            findViewById<TextView>(R.id.description).text = card.action

            findViewById<Button?>(R.id.button).setOnClickListener {
                //TODO: Execute button action
            }

            //Remove second button if action is inevitable
            if(card.choices.size == 1) {
                val button2 = findViewById<Button>(R.id.button2)
                val parent = button2.parent as? ViewGroup
                parent?.removeView(button2)

            } else {
                findViewById<Button?>(R.id.button2).setOnClickListener {
                    //TODO: Execute button action
                }
            }
        }
    }
}