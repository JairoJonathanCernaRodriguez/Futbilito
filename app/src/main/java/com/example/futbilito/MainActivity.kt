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
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var _x by mutableStateOf(0f)
    private var _y by mutableStateOf(0f)
    private var scoreLocal by mutableStateOf(0)
    private var scoreVisitante by mutableStateOf(0)

    private var velocityX by mutableStateOf(0f) // Velocidad en el eje X
    private var velocityY by mutableStateOf(0f) // Velocidad en el eje Y

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
            val ax = it.values[0] // Aceleración en X
            val ay = it.values[1] // Aceleración en Y

            // Actualizar las velocidades con base en la aceleración
            velocityX -= ax * 5
            velocityY += ay * 5

            // Limitar la velocidad máxima
            velocityX = max(-20f, min(20f, velocityX))
            velocityY = max(-20f, min(20f, velocityY))

            // Actualizar la posición de la pelota
            _x += velocityX
            _y += velocityY

            // Detectar rebote en los bordes (izquierda, derecha, arriba, abajo)
            if (_x < -450f) { // Limitar al borde izquierdo
                _x = -450f
                velocityX = -velocityX
            } else if (_x > 450f) { // Limitar al borde derecho
                _x = 450f
                velocityX = -velocityX
            }

            if (_y < -420f) { // Limitar al borde superior
                _y = -420f
                velocityY = -velocityY
            } else if (_y > 990f) { // Limitar al borde inferior
                _y = 990f
                velocityY = -velocityY
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun FutbolitoGame(x: Float, y: Float, scoreLocal: Int, scoreVisitante: Int, onLocalGoal: () -> Unit, onVisitanteGoal: () -> Unit) {
    var isBallInLocalGoal by remember { mutableStateOf(false) }
    var isBallInVisitanteGoal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de la cancha con el marcador encima
        Box(modifier = Modifier.fillMaxSize()) {

            // Imagen de la cancha
            Image(
                painter = painterResource(id = R.drawable.cancha_con_marcador),
                contentDescription = "Cancha de fútbol",
                modifier = Modifier.fillMaxSize() // La imagen ocupa todo el espacio
            )

            // Dibujo del marcador sobre la cancha
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 53.dp) // Ajusta la posición general del marcador
                    .align(Alignment.TopCenter) // Alineamos el marcador al centro superior
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Marcador para el equipo local, ajusta su posición si es necesario
                    Column(
                        modifier = Modifier.offset(x = 20.dp) // Mueve el marcador del local a la izquierda
                        , horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LOCAL", fontSize = 20.sp, color = Color.Yellow)
                        Text("$scoreLocal", fontSize = 40.sp, color = Color.Yellow)
                    }
                    // Marcador para el equipo visitante, ajusta su posición si es necesario
                    Column(
                        modifier = Modifier.offset(x = 0.dp) // Mueve el marcador del visitante a la derecha
                        , horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VISITANTE", fontSize = 18.sp, color = Color.Yellow)
                        Text("$scoreVisitante", fontSize = 40.sp, color = Color.Yellow)
                    }
                }
            }

            // Canvas para las porterías y la pelota
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ballRadius = 7.dp.toPx()

                // Ajuste del tamaño de las porterías
                val goalWidth = 15.dp.toPx() // Ajustado para hacerlas más grandes
                val goalHeight = 15.dp.toPx() // Ajustado para hacerlas más grandes

                // Calculamos el desplazamiento de las porterías
                val goalOffsetTop = 225.dp.toPx() // Desplazamiento de la portería superior
                val goalOffsetBottom = 20.dp.toPx() // Desplazamiento de la portería inferior

                // Coordenadas de la portería superior
                val goalXStart = (size.width - goalWidth) / 2
                val goalXEnd = goalXStart + goalWidth

                // Dibujar las porterías
                drawRect(Color.Black, topLeft = Offset(goalXStart, goalOffsetTop), size = Size(goalWidth, goalHeight))
                drawRect(Color.Black, topLeft = Offset(goalXStart, size.height - goalHeight - goalOffsetBottom), size = Size(goalWidth, goalHeight))

                // Posición de la pelota
                val ballPosition = Offset(size.width / 2 + x, size.height / 2 + y)
                val isBallInsideGoalX = ballPosition.x in goalXStart..goalXEnd
                val isBallInLocal = isBallInsideGoalX && (ballPosition.y - ballRadius <= goalOffsetTop + goalHeight)
                val isBallInVisitante = isBallInsideGoalX && (ballPosition.y + ballRadius >= size.height - goalOffsetBottom - goalHeight)

                // Detectar gol solo si la pelota acaba de entrar
                if (isBallInLocal && !isBallInLocalGoal) {
                    onLocalGoal()
                    isBallInLocalGoal = true
                } else if (!isBallInLocal) {
                    isBallInLocalGoal = false
                }

                if (isBallInVisitante && !isBallInVisitanteGoal) {
                    onVisitanteGoal()
                    isBallInVisitanteGoal = true
                } else if (!isBallInVisitante) {
                    isBallInVisitanteGoal = false
                }

                // Dibujar la pelota
                drawCircle(
                    color = Color(0.906f, 0.275f, 0.843f, 1.0f), // Rosa brillante
                    radius = ballRadius,
                    center = ballPosition
                )
            }
        }
    }
}


