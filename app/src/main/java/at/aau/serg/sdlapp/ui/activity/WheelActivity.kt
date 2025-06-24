package at.aau.serg.sdlapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WheelActivity() : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dice = intent.getIntExtra("dice", -1)
        setContent {
            WheelScreen(dice)
        }
    }

    @Composable
    fun WheelScreen(dice: Int) {
        var rotationAnim = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        var isSpinning by remember { mutableStateOf(false) }
        Surface(color = Color.Companion.Transparent, tonalElevation = 0.dp) {

            Column(
                modifier = Modifier.Companion.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Companion.Center // Zeiger & Rad zentrieren
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.gluecksrad),
                        contentDescription = "Glücksrad",
                        modifier = Modifier.Companion
                            .size(250.dp)
                            .rotate(rotationAnim.value)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.zeiger),
                        contentDescription = "Zeiger",
                        modifier = Modifier.Companion
                            .size(40.dp)
                            .offset(x = (120).dp)
                    )
                }

                Spacer(modifier = Modifier.Companion.height(16.dp))

                Button(
                    enabled = !isSpinning,
                    onClick = {
                        isSpinning = true
                        spinWheel(dice, scope, rotationAnim, onSpinEnd = {
                            isSpinning = false
                            finish()
                        })

                    }
                ) {
                    Text("Drehen")
                }
            }
        }
    }

    fun spinWheel(
        dice: Int,
        scope: CoroutineScope,
        rotationAnim: Animatable<Float, AnimationVector1D>,
        onSpinEnd: () -> Unit,
    ) {
        scope.launch {
            val angles =
                floatArrayOf(144f, 108f, 72f, 36f, 0f, 324f, 288f, 252f, 216f, 180f)
            val angle = 1080f + angles[dice - 1]
            Log.d("Gluecksrad", "Gewürfelt: $dice")
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + angle,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                )
            )
            delay(1500)
            resetWheel(rotationAnim)
            onSpinEnd()
        }

    }

    suspend fun resetWheel(rotationAngle: Animatable<Float, AnimationVector1D>) {
        var resetAngle = rotationAngle.value.toInt() / 360 + 1
        if (rotationAngle.value.toInt() % 360 == 0) {
            rotationAngle.animateTo(
                targetValue = 360f * resetAngle - 360f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            rotationAngle.animateTo(
                targetValue = 360f * resetAngle,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }

    }
}