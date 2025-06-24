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
    var relationship: Boolean = false,
    var color: CarColor = CarColor.BLUE,
    var startedWithUniversity: Boolean = false,

    ) {

    init{
        setCarColor()
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

    //setzt die CarColor je nach ID, bei 0 bleibt Standardwert
    fun setCarColor() {
        when (id) {
            "1" -> {
                color = CarColor.GREEN
            }
            "2" -> {
                color = CarColor.RED
            }
            "3" -> {
                color = CarColor.YELLOW
            }
        }
    }
}

/**
 * Definiert die verfügbaren Auto-Farben
 */
enum class CarColor {
    BLUE, GREEN, RED, YELLOW
}
