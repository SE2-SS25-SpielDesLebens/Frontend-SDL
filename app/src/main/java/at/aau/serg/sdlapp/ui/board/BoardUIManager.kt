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
import at.aau.serg.sdlapp.model.player.CarColor
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
) {    /**
     * Zeigt zwei aufeinanderfolgende Dialoge:
     * 1. Dialog zur Auswahl der Spielerfarbe
     * 2. Dialog zur Auswahl des Startpunktes (normal oder Uni)
     */
    fun showStartChoiceDialog(playerName: String, stompClient: StompConnectionManager) {
        showColorSelectionDialog(playerName, stompClient)
    }
    
    /**
     * Erster Dialog: Auswahl der Spielerfarbe
     */
    private fun showColorSelectionDialog(playerName: String, stompClient: StompConnectionManager) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_color_selection, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Radiobuttons für die Farbauswahl
        val redRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.rbRed)
        val blueRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.rbBlue)
        val greenRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.rbGreen)
        val yellowRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.rbYellow)
        
        // Vorschau-Bild für die ausgewählte Farbe
        val previewImage = dialogView.findViewById<android.widget.ImageView>(R.id.ivColorPreview)

        // Standardfarbe auf Blau setzen
        var selectedColor = CarColor.BLUE
        previewImage.setImageResource(R.drawable.car_blue_0)

        // Farbauswahl-Listener
        redRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedColor = CarColor.RED
                previewImage.setImageResource(R.drawable.car_red_0)
                println("🎨 Farbe Rot ausgewählt")
            }
        }
        
        blueRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedColor = CarColor.BLUE
                previewImage.setImageResource(R.drawable.car_blue_0)
                println("🎨 Farbe Blau ausgewählt")
            }
        }
        
        greenRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedColor = CarColor.GREEN
                previewImage.setImageResource(R.drawable.car_green_0)
                println("🎨 Farbe Grün ausgewählt")
            }
        }
        
        yellowRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedColor = CarColor.YELLOW
                previewImage.setImageResource(R.drawable.car_yellow_0)
                println("🎨 Farbe Gelb ausgewählt")
            }
        }

        // Bestätigungsbutton
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmColor)

        // Bestätigungsbutton Listener
        confirmButton.setOnClickListener {
            try {
                println("🎨 Farbe $selectedColor bestätigt")
                
                // Setze Farbe des Spielers
                val localPlayerId = playerManager.getLocalPlayer()?.id ?: ""
                playerManager.setLocalPlayer(localPlayerId, selectedColor)
                
                // Sende Farbe an Backend
                stompClient.sendColorSelection(playerName, selectedColor.name)
                
                // Zeige Bestätigung
                Toast.makeText(context, "Farbe $selectedColor ausgewählt", Toast.LENGTH_SHORT).show()
                
                // Schließe diesen Dialog und öffne den Startpunkt-Dialog
                dialog.dismiss()
                println("🎨 Farbauswahl-Dialog geschlossen")
                
                // Zweiter Dialog für die Startpunktauswahl anzeigen
                showPositionSelectionDialog(playerName, stompClient, selectedColor)
                
            } catch (e: Exception) {
                println("❌❌❌ Fehler bei der Farbauswahl: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }

        // Dialog anzeigen
        dialog.show()
    }
      /**
     * Zweiter Dialog: Auswahl des Startpunktes
     */
    private fun showPositionSelectionDialog(playerName: String, stompClient: StompConnectionManager, selectedColor: CarColor) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_position_selection, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        // Vorschau der gewählten Farbe anzeigen
        val colorPreviewImage = dialogView.findViewById<android.widget.ImageView>(R.id.ivPositionColorPreview)
        when (selectedColor) {
            CarColor.RED -> colorPreviewImage.setImageResource(R.drawable.car_red_0)
            CarColor.BLUE -> colorPreviewImage.setImageResource(R.drawable.car_blue_0)
            CarColor.GREEN -> colorPreviewImage.setImageResource(R.drawable.car_green_0)
            CarColor.YELLOW -> colorPreviewImage.setImageResource(R.drawable.car_yellow_0)
        }

        // Buttons für die Startpunktauswahl
        val normalButton = dialogView.findViewById<Button>(R.id.btnStartNormal)
        val uniButton = dialogView.findViewById<Button>(R.id.btnStartUni)

        // Normal-Start Button
        normalButton.setOnClickListener {
            try {
                println("🎮 Normal-Start Button geklickt")
                // Starte am normalen Startfeld (Index 1)
                val startFieldIndex = 1
                
                // Zeige Zusammenfassung der Auswahl
                Toast.makeText(context, "Du spielst mit Farbe $selectedColor und startest normal", Toast.LENGTH_SHORT).show()
                
                // Schließe diesen Dialog und öffne den Spielerreihenfolge-Dialog
                dialog.dismiss()
                println("🎮 Startpositon-Dialog geschlossen")
                
                // Dritter Dialog für die Spielerreihenfolge anzeigen
                showPlayerOrderDialog(playerName, stompClient, selectedColor, startFieldIndex)
                
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Normal-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
                // Trotz Fehler die Startfeld-Auswahl übernehmen
                uiCallbacks.onStartFieldSelected(1)
            }
        }

        // Uni-Start Button
        uniButton.setOnClickListener {
            try {
                println("🎓 Uni-Start Button geklickt")
                // Starte am Uni-Startfeld (Index 10)
                val startFieldIndex = 10
                
                // Zeige Zusammenfassung der Auswahl
                Toast.makeText(context, "Du spielst mit Farbe $selectedColor und startest an der Uni", Toast.LENGTH_SHORT).show()
                
                // Schließe diesen Dialog und öffne den Spielerreihenfolge-Dialog
                dialog.dismiss()
                println("� Startposition-Dialog geschlossen")
                
                // Dritter Dialog für die Spielerreihenfolge anzeigen
                showPlayerOrderDialog(playerName, stompClient, selectedColor, startFieldIndex)
                
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Uni-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
                // Trotz Fehler die Startfeld-Auswahl übernehmen
                uiCallbacks.onStartFieldSelected(10)
            }
        }

        // Dialog anzeigen
        dialog.show()
    }
    
    /**
     * Dritter Dialog: Auswahl der Spielerreihenfolge
     */
    private fun showPlayerOrderDialog(playerName: String, stompClient: StompConnectionManager, selectedColor: CarColor, startFieldIndex: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_order_selection, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        // Vorschau der gewählten Farbe anzeigen
        val colorPreviewImage = dialogView.findViewById<android.widget.ImageView>(R.id.ivOrderColorPreview)
        when (selectedColor) {
            CarColor.RED -> colorPreviewImage.setImageResource(R.drawable.car_red_0)
            CarColor.BLUE -> colorPreviewImage.setImageResource(R.drawable.car_blue_0)
            CarColor.GREEN -> colorPreviewImage.setImageResource(R.drawable.car_green_0)
            CarColor.YELLOW -> colorPreviewImage.setImageResource(R.drawable.car_yellow_0)
        }
        
        // Statustext für bereits gewählte Positionen
        val selectedPositionsText = dialogView.findViewById<TextView>(R.id.tvSelectedPositions)
        
        // Positionsbuttons
        val btnPosition1 = dialogView.findViewById<Button>(R.id.btnPosition1)
        val btnPosition2 = dialogView.findViewById<Button>(R.id.btnPosition2)
        val btnPosition3 = dialogView.findViewById<Button>(R.id.btnPosition3)
        val btnPosition4 = dialogView.findViewById<Button>(R.id.btnPosition4)
        
        // Map für gewählte Positionen
        val chosenPositions = mutableMapOf<String, Int>()
        
        // Abonniere Spielerreihenfolge-Updates
        stompClient.onPlayerOrdersReceived = { updatedOrders ->
            Handler(Looper.getMainLooper()).post {
                // Update der gewählten Positionen
                chosenPositions.clear()
                chosenPositions.putAll(updatedOrders)
                
                // Update der UI
                updatePositionButtonStates(
                    chosenPositions,
                    btnPosition1,
                    btnPosition2,
                    btnPosition3,
                    btnPosition4,
                    selectedPositionsText
                )
            }
        }
        
        // Initial abonnieren
        stompClient.subscribeToPlayerOrders()
        
        // Button-Listener einrichten
        btnPosition1.setOnClickListener {
            handlePositionSelection(playerName, stompClient, selectedColor, startFieldIndex, 1, dialog, chosenPositions)
        }
        
        btnPosition2.setOnClickListener {
            handlePositionSelection(playerName, stompClient, selectedColor, startFieldIndex, 2, dialog, chosenPositions)
        }
        
        btnPosition3.setOnClickListener {
            handlePositionSelection(playerName, stompClient, selectedColor, startFieldIndex, 3, dialog, chosenPositions)
        }
        
        btnPosition4.setOnClickListener {
            handlePositionSelection(playerName, stompClient, selectedColor, startFieldIndex, 4, dialog, chosenPositions)
        }
        
        // Dialog anzeigen
        dialog.show()
    }
    
    /**
     * Aktualisiert den Zustand der Positions-Buttons basierend auf bereits gewählten Positionen
     */
    private fun updatePositionButtonStates(
        chosenPositions: Map<String, Int>,
        btnPosition1: Button,
        btnPosition2: Button,
        btnPosition3: Button,
        btnPosition4: Button,
        statusText: TextView
    ) {
        // Deaktiviere bereits gewählte Positionen
        btnPosition1.isEnabled = !chosenPositions.containsValue(1)
        btnPosition2.isEnabled = !chosenPositions.containsValue(2)
        btnPosition3.isEnabled = !chosenPositions.containsValue(3)
        btnPosition4.isEnabled = !chosenPositions.containsValue(4)
        
        // Button-Farben aktualisieren
        btnPosition1.alpha = if (btnPosition1.isEnabled) 1.0f else 0.5f
        btnPosition2.alpha = if (btnPosition2.isEnabled) 1.0f else 0.5f
        btnPosition3.alpha = if (btnPosition3.isEnabled) 1.0f else 0.5f
        btnPosition4.alpha = if (btnPosition4.isEnabled) 1.0f else 0.5f
        
        // Status-Text aktualisieren
        if (chosenPositions.isEmpty()) {
            statusText.text = "Bereits gewählte Positionen: -"
        } else {
            val positionsText = chosenPositions.entries.joinToString(", ") { 
                "${it.key}: Position ${it.value}" 
            }
            statusText.text = "Bereits gewählte Positionen:\n$positionsText"
        }
    }
    
    /**
     * Behandelt die Auswahl einer Spielerposition
     */
    private fun handlePositionSelection(
        playerName: String,
        stompClient: StompConnectionManager,
        selectedColor: CarColor,
        startFieldIndex: Int,
        position: Int,
        dialog: AlertDialog,
        chosenPositions: Map<String, Int>
    ) {
        try {
            println("🎲 Spielerposition $position ausgewählt")
            
            // Sende gewählte Position an Server
            stompClient.sendPlayerOrder(playerName, position)
            
            // Bestätigungsnachricht zeigen
            Toast.makeText(context, "Du spielst auf Position $position", Toast.LENGTH_SHORT).show()
            
            // Benachrichtige über Startfeld-Auswahl (jetzt erst hier, am Ende des gesamten Auswahlprozesses)
            uiCallbacks.onStartFieldSelected(startFieldIndex)
            
            // Dialog schließen
            dialog.dismiss()
            println("� Spielerreihenfolge-Dialog geschlossen")
            
        } catch (e: Exception) {
            println("❌❌❌ Fehler bei der Positionsauswahl: ${e.message}")
            e.printStackTrace()
            
            // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
            dialog.dismiss()
            
            // Benachrichtige über Startfeld-Auswahl trotz Fehler
            uiCallbacks.onStartFieldSelected(startFieldIndex)
        }
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
    }    /**
     * Zeigt eine Benachrichtigung über einen neuen Spieler an
     */
    fun showNewPlayerNotification(playerId: String) {
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
        val players = playerManager.getAllPlayersAsList()
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
        val players = playerManager.getAllPlayersAsList()

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
