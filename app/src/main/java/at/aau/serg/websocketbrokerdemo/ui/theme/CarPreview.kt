package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.Car
import at.aau.serg.websocketbrokerdemo.CarColor
import at.aau.serg.websocketbrokerdemo.getCarImageRes

@Composable
fun CarPreview(car: Car) {
    val imageRes = getCarImageRes(car.color, car.passengers)

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Car ${car.color}",
        modifier = Modifier
            .width(140.dp)
            .padding(16.dp)
    )
}

@Preview(name = "Yellow Car")
@Composable
fun PreviewYellowCar() {
    MyApplicationTheme {
        CarPreview(Car(id = 1, color = CarColor.YELLOW, passengers = 3))
    }
}

@Preview(name = "Red Car")
@Composable
fun PreviewRedCar() {
    MyApplicationTheme {
        CarPreview(Car(id = 2, color = CarColor.RED, passengers = 3))
    }
}

@Preview(name = "Green Car")
@Composable
fun PreviewGreenCar() {
    MyApplicationTheme {
        CarPreview(Car(id = 3, color = CarColor.GREEN, passengers = 3))
    }
}

@Preview(name = "Blue Car")
@Composable
fun PreviewBlueCar() {
    MyApplicationTheme {
        CarPreview(Car(id = 4, color = CarColor.BLUE, passengers = 3))
    }
}
