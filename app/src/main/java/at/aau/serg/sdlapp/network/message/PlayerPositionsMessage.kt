package at.aau.serg.sdlapp.network.message

import com.google.gson.annotations.SerializedName

/**
 * Repräsentiert eine Nachricht mit den Positionen aller Spieler auf dem Spielbrett.
 * Diese wird verwendet, um die Positionen zwischen den Clients zu synchronisieren.
 *
 * @property playerPositions Map von Spieler-IDs zu Feldindizes
 * @property timestamp Zeitstempel der Nachricht
 * @property type Nachrichtentyp (immer "playerPositions")
 */
data class PlayerPositionsMessage(
    @SerializedName("playerPositions") val playerPositions: Map<String, Int>,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("type") val type: String = "playerPositions"
)