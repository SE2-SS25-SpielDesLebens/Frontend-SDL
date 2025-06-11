package at.aau.serg.sdlapp.model.player

import at.aau.serg.sdlapp.R

/**
 * Repräsentiert einen Spieler im Spiel
 */
data class Player(
    val id: String,
    val name: String,
    var currentFieldIndex: Int = 0,

    // ➕ Zusätzliche Felder für Statistiken
    var money: Int = 0,
    var children: Int = 0,
    var hasEducation: Boolean = false,
    var investments: Int = 0,
    var salary: Int = 0,                   // Standardwert
    val relationship: Boolean = false,
) {
    // Farbe basierend auf der ID bestimmen
    val color: CarColor = when (id.toIntOrNull()?.rem(4) ?: 0) {
        0 -> CarColor.BLUE
        1 -> CarColor.GREEN
        2 -> CarColor.RED
        3 -> CarColor.YELLOW
        else -> CarColor.BLUE
    }


    // Liefert die Ressourcen-ID des Auto-Bildes
    fun getCarImageResource(): Int {
        return when (color) {
            CarColor.BLUE -> R.drawable.car_blue_0
            CarColor.GREEN -> R.drawable.car_green_0
            CarColor.RED -> R.drawable.car_red_0
            CarColor.YELLOW -> R.drawable.car_yellow_0
        }
    }
}

/**
 * Definiert die verfügbaren Auto-Farben
 */
enum class CarColor {
    BLUE, GREEN, RED, YELLOW
}
