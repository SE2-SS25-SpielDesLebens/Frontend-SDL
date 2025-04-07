package at.aau.serg.websocketbrokerdemo

enum class CarColor {
    YELLOW, RED, GREEN, BLUE
}

data class Car(
    val id: Int,
    val color: CarColor,
    var passengers: Int
)
