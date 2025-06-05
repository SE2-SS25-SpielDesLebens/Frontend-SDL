package at.aau.serg.sdlapp.ui.board

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.player.Player
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager

/**
 * Verwaltet die UI-Elemente der BoardActivity wie Dialoge, Overlays, etc.
 */
class BoardUIManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val layoutInflater: LayoutInflater,
    private val uiCallbacks: UICallbacks
) {
    /**
     * Zeigt einen Dialog zur Auswahl des Startpunktes (normal oder Uni)
     */    fun showStartChoiceDialog(playerName: String, stompClient: StompConnectionManager) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_start_choice, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Statustext für Startpunkt-Auswahl
        val statusText = dialogView.findViewById<TextView>(R.id.tvStatus)
        statusText?.text = "Wähle deinen Startpunkt."

        // Buttons für die Startpunktauswahl
        val normalButton = dialogView.findViewById<Button>(R.id.btnStartNormal)
        val uniButton = dialogView.findViewById<Button>(R.id.btnStartUni)

        // Buttons sind immer aktiv, da die Verbindung bereits in der LobbyActivity hergestellt wurde
        normalButton.isEnabled = true
        uniButton.isEnabled = true

        // Normal-Start Button
        normalButton.setOnClickListener {
            try {
                println("🎮 Normal-Start Button geklickt")
                // Starte am normalen Startfeld (Index 1)
                val startFieldIndex = 1
                
                uiCallbacks.onStartFieldSelected(startFieldIndex)

                // Schließe den Dialog
                dialog.dismiss()
                println("🎮 Dialog geschlossen")
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Normal-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }

        // Uni-Start Button
        uniButton.setOnClickListener {
            try {
                println("🎓 Uni-Start Button geklickt")
                // Starte am Uni-Startfeld (Index 10)
                val startFieldIndex = 10
                
                uiCallbacks.onStartFieldSelected(startFieldIndex)

                // Schließe den Dialog
                dialog.dismiss()
                println("🎓 Dialog geschlossen")
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Uni-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }

        // Dialog anzeigen
        dialog.show()
    }

    /**
     * Zeigt einen Fehlerdialog mit Titel und Nachricht an.
     */
    fun showErrorDialog(title: String, message: String) {
        Handler(Looper.getMainLooper()).post {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show()

            println("❌ Fehlerdialog angezeigt: $title - $message")
        }
    }

    /**
     * Zeigt eine Benachrichtigung über einen neuen Spieler an
     */
    fun showNewPlayerNotification(playerId: Int) {
        Toast.makeText(
            context,
            "Neuer Spieler beigetreten: Spieler $playerId",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Zeigt eine Benachrichtigung über entfernte Spieler an
     */
    fun showRemovedPlayersNotification(removedPlayers: List<String>) {
        if (removedPlayers.size == 1) {
            Toast.makeText(
                context,
                "Spieler ${removedPlayers[0]} hat das Spiel verlassen",
                Toast.LENGTH_SHORT
            ).show()
        } else if (removedPlayers.size > 1) {
            Toast.makeText(
                context,
                "${removedPlayers.size} Spieler haben das Spiel verlassen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Zeigt alle aktiven Spieler mit ihren Positionen an
     * Nützlich für Debug-Zwecke oder als Info für den Benutzer
     */
    fun showActivePlayersInfo() {
        val players = playerManager.getAllPlayers()
        if (players.isEmpty()) {
            println("👥 Keine Spieler vorhanden")
            return
        }

        println("👥 Aktive Spieler (${players.size}):")
        players.forEach { player ->
            val isLocal = if (playerManager.isLocalPlayer(player.id)) " (Du)" else ""
            println("   👤 Spieler ${player.id}${isLocal}: Farbe=${player.color}, Position=${player.currentFieldIndex}")
        }

        // Optional: Zeige eine Benachrichtigung mit der Anzahl der Spieler
        if (players.size > 1) {
            val otherPlayersCount = players.size - 1
            val message = "Es sind insgesamt ${players.size} Spieler online"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Zeigt ein UI mit allen aktiven Spielern an
     * Diese Methode erstellt ein Overlay mit der Spielerliste
     */
    fun showPlayerListOverlay() {
        // Spielerliste abrufen
        val players = playerManager.getAllPlayers()

        // Dialog erstellen
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_list, null)
        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_MinWidth)
            .setView(dialogView)
            .setTitle("Aktive Spieler")
            .setCancelable(true)
            .create()

        // Spielerliste-Layout finden
        val playerListLayout = dialogView.findViewById<LinearLayout>(R.id.playerListLayout)

        // Spieler anzeigen oder Hinweis, wenn keine Spieler vorhanden sind
        if (players.isEmpty()) {
            val emptyView = TextView(context)
            emptyView.text = "Keine Spieler verbunden."
            emptyView.gravity = android.view.Gravity.CENTER
            playerListLayout.addView(emptyView)
        } else {
            // Für jeden Spieler einen Eintrag erstellen
            for (player in players) {
                val playerItemView = layoutInflater.inflate(R.layout.item_player, null)

                // Views finden und befüllen
                val nameTextView = playerItemView.findViewById<TextView>(R.id.playerNameTextView)
                val idTextView = playerItemView.findViewById<TextView>(R.id.playerIdTextView)
                val colorImageView = playerItemView.findViewById<android.widget.ImageView>(R.id.playerColorImageView)
                val positionTextView = playerItemView.findViewById<TextView>(R.id.playerPositionTextView)

                // Daten setzen
                nameTextView.text = player.name
                idTextView.text = "ID: ${player.id}"
                colorImageView.setImageResource(player.getCarImageResource())
                positionTextView.text = "Feld: ${player.currentFieldIndex}"

                // Lokalen Spieler hervorheben
                if (playerManager.isLocalPlayer(player.id)) {
                    nameTextView.setTypeface(nameTextView.typeface, android.graphics.Typeface.BOLD)
                    nameTextView.text = "${nameTextView.text} (Du)"
                }

                // Zum Layout hinzufügen
                playerListLayout.addView(playerItemView)
            }
        }

        // Dialog anzeigen
        dialog.show()
    }

    /**
     * Aktualisiert den Status-Text mit der Anzahl der aktiven Spieler
     */
    fun updateStatusText(statusText: TextView) {
        val players = playerManager.getAllPlayers()
        val count = players.size
        
        statusText.text = when {
            count == 0 -> "Keine Spieler online"
            count == 1 -> "1 Spieler online"
            else -> "$count Spieler online"
        }

        // Animation für Statusänderung
        val animation = AlphaAnimation(0.5f, 1.0f)
        animation.duration = 500
        animation.fillAfter = true
        statusText.startAnimation(animation)
    }

    /**
     * Zeigt eine Benachrichtigung über andere Spieler an
     */
    fun showOtherPlayersNotification(allPlayers: List<Player>, hasChanges: Boolean) {
        if (allPlayers.size > 1 && hasChanges) {
            val otherPlayersCount = allPlayers.size - 1
            val message = "Es ${ if(otherPlayersCount == 1) "ist" else "sind" } $otherPlayersCount andere${ if(otherPlayersCount == 1) "r" else "" } Spieler online"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Interface für die UI-Callbacks
     */
    fun interface UICallbacks {
        fun onStartFieldSelected(fieldIndex: Int)
    }
}
