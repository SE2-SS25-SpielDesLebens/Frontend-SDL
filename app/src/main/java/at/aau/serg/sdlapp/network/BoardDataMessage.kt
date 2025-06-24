package at.aau.serg.sdlapp.network

import at.aau.serg.sdlapp.model.board.Field
import com.google.gson.annotations.SerializedName

/**
 * Repräsentiert eine Nachricht mit den Spielbrettdaten vom Server.
 *
 * @property fields Die Liste aller Felder des Spielbretts
 * @property timestamp Der Zeitstempel der Daten
 */
data class BoardDataMessage(
    @SerializedName("fields") val fields: List<FieldDto>,
    @SerializedName("timestamp") val timestamp: String
)

/**
 * Data Transfer Object für ein Feld, entspricht dem Backend-Format
 */
data class FieldDto(
    @SerializedName("index") val index: Int,
    @SerializedName("x") val x: Double,
    @SerializedName("y") val y: Double,
    @SerializedName("nextFields") val nextFields: List<Int>,
    @SerializedName("type") val type: String
) {
    // Konvertiere FieldDto zu lokalem Field-Model
    fun toField(): Field {
        val fieldType = try {
            at.aau.serg.sdlapp.model.board.FieldType.valueOf(type)
        } catch (e: Exception) {
            // Fallback zum AKTION Typ, falls unbekannter Typ
            at.aau.serg.sdlapp.model.board.FieldType.AKTION
        }
        
        return Field(
            index = index,
            x = x.toFloat(),
            y = y.toFloat(),
            nextFields = nextFields,
            type = fieldType
        )
    }
}
