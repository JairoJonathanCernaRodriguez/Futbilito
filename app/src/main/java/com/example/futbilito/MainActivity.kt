package com.example.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbilito.R
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var _x by mutableStateOf(0f)
    private var _y by mutableStateOf(0f)
    private var scoreLocal by mutableStateOf(0)
    private var scoreVisitante by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            FutbolitoGame(_x, _y, scoreLocal, scoreVisitante,
                onLocalGoal = { scoreLocal++ },
                onVisitanteGoal = { scoreVisitante++ }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val ax = it.values[0]
            val ay = it.values[1]

            // Aumentar margen en horizontal y reducir en vertical
            _x = max(-500f, min(500f, _x - ax * 5))
            _y = max(-700f, min(700f, _y + ay * 5))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun FutbolitoGame(x: Float, y: Float, scoreLocal: Int, scoreVisitante: Int, onLocalGoal: () -> Unit, onVisitanteGoal: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Marcador estilo imagen proporcionada
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOCAL", fontSize = 20.sp, color = Color.Black)
                    Text("$scoreLocal", fontSize = 36.sp, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VISITANTE", fontSize = 20.sp, color = Color.Black)
                    Text("$scoreVisitante", fontSize = 36.sp, color = Color.Black)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.cancha_futbol), // Usa un recurso válido de imagen
                    contentDescription = "Cancha de fútbol",
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val ballRadius = 7.dp.toPx()
                    val goalWidth = ballRadius * 2
                    val goalHeight = ballRadius * 2
                    val goalOffset = 80.dp.toPx() // Mueve las porterías más hacia adent

                    // Dibujar las porterías más adentro
                    drawRect(Color.Black, topLeft = Offset((size.width - goalWidth) / 2, goalOffset), size = Size(goalWidth, goalHeight))
                    drawRect(Color.Black, topLeft = Offset((size.width - goalWidth) / 2, size.height - goalHeight - goalOffset), size = Size(goalWidth, goalHeight))

                    val ballPosition = Offset(size.width / 2 + x, size.height / 2 + y)

                    // Verificar si la pelota entra en la portería ajustada
                    if (ballPosition.y - ballRadius <= goalHeight + goalOffset) {
                        onLocalGoal()
                    } else if (ballPosition.y + ballRadius >= size.height - goalHeight - goalOffset) {
                        onVisitanteGoal()
                    }

                    // Dibujar la pelota
                    drawCircle(
                        color = Color.Blue,
                        radius = ballRadius, // Pelota de tamaño 7.dp
                        center = ballPosition
                    )
                }
            }
        }
    }
}