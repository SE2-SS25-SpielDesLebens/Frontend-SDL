package at.aau.serg.websocketbrokerdemo

fun getCarImageRes(color: CarColor, passengers: Int): Int {
    val capped = passengers.coerceIn(0, 6)
    return when (color) {
        CarColor.YELLOW -> when (capped) {
            0 -> R.drawable.car_yellow_0
            1 -> R.drawable.car_yellow_1
            2 -> R.drawable.car_yellow_2
            3 -> R.drawable.car_yellow_3
            4 -> R.drawable.car_yellow_4
            5 -> R.drawable.car_yellow_5
            6 -> R.drawable.car_yellow_6
            else -> R.drawable.car_yellow_0
        }
        CarColor.RED -> when (capped) {
            0 -> R.drawable.car_red_0
            1 -> R.drawable.car_red_1
            2 -> R.drawable.car_red_2
            3 -> R.drawable.car_red_3
            4 -> R.drawable.car_red_4
            5 -> R.drawable.car_red_5
            6 -> R.drawable.car_red_6
            else -> R.drawable.car_red_0
        }
        CarColor.GREEN -> when (capped) {
            0 -> R.drawable.car_green_0
            1 -> R.drawable.car_green_1
            2 -> R.drawable.car_green_2
            3 -> R.drawable.car_green_3
            4 -> R.drawable.car_green_4
            5 -> R.drawable.car_green_5
            6 -> R.drawable.car_green_6
            else -> R.drawable.car_green_0
        }
        CarColor.BLUE -> when (capped) {
            0 -> R.drawable.car_blue_0
            1 -> R.drawable.car_blue_1
            2 -> R.drawable.car_blue_2
            3 -> R.drawable.car_blue_3
            4 -> R.drawable.car_blue_4
            5 -> R.drawable.car_blue_5
            6 -> R.drawable.car_blue_6
            else -> R.drawable.car_blue_0
        }
    }
}
