package at.aau.serg.sdlapp.network.message

import com.google.gson.annotations.SerializedName

/**
 * Repräsentiert eine Nachricht mit den gewählten Spielerreihenfolge-Positionen.
 * Wird verwendet, um allen Spielern mitzuteilen, welche Positionen bereits vergeben sind.
 *
 * @property playerOrders Map von Spieler-IDs zu gewählten Positionen (1-4)
 * @property timestamp Zeitstempel der Nachricht
 * @property type Nachrichtentyp (immer "playerOrders")
 */
data class PlayerOrderMessage(
    @SerializedName("playerOrders") val playerOrders: Map<String, Int>,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("type") val type: String = "playerOrders"
)
