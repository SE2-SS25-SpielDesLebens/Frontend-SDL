package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.Car
import at.aau.serg.websocketbrokerdemo.getCarImageRes

@Composable
fun CarView(car: Car, onPassengerChange: (Int) -> Unit) {
    val imageRes = getCarImageRes(car.color, car.passengers)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Auto ${car.color} mit ${car.passengers} Pins",
            modifier = Modifier.size(160.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onPassengerChange(car.passengers - 1) },
                enabled = car.passengers > 0
            ) {
                Text("-")
            }
            Text("${car.passengers}")
            Button(
                onClick = { onPassengerChange(car.passengers + 1) },
                enabled = car.passengers < 6
            ) {
                Text("+")
            }
        }
    }
}
