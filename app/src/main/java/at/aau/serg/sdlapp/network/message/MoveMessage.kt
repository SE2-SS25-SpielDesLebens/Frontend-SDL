package at.aau.serg.sdlapp.network.message

import at.aau.serg.sdlapp.model.board.FieldType
import com.google.gson.annotations.SerializedName

/**
 * Repräsentiert eine Bewegungsnachricht vom Server.
 *
 * @property playerName Der Name des Spielers, der sich bewegt hat
 * @property fieldIndex Der Index des Feldes, auf dem der Spieler jetzt steht
 * @property type Der Typ des Feldes als String (z.B. "AKTION", "ZAHLTAG", etc.)
 * @property timestamp Der Zeitstempel der Bewegung
 * @property nextPossibleFields Liste der möglichen nächsten Felder, auf die sich der Spieler bewegen kann
 */
data class MoveMessage(
    @SerializedName("playerName") val playerName: String,
    @SerializedName("index") val fieldIndex: Int, 
    @SerializedName("type") val typeString: String,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("nextPossibleFields") val nextPossibleFields: List<Int> = emptyList(),
    @SerializedName("passedFields") val passedFields: List<Int> = emptyList()
) {
    val playerId: String
        get() = playerName
    
    // Konvertiert den empfangenen String-Feldtyp in einen Enum-Wert
    val fieldType: FieldType
        get() = try {
            FieldType.valueOf(typeString)
        } catch (e: Exception) {
            // Fallback zum AKTION Typ, falls unbekannter Typ
            FieldType.AKTION
        }
}
