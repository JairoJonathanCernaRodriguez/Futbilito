package com.example.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.futbilito.ui.Cronometro
import com.example.futbilito.ui.OverlayMessage
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var _x by mutableStateOf(0f)
    private var _y by mutableStateOf(0f)
    private var velocityX by mutableStateOf(0f)
    private var velocityY by mutableStateOf(0f)
    private var gameState by mutableStateOf(GameState.PLAYING)
    private val items = mutableStateListOf<Item>()

    private val gameAreaTopMargin = 80f
    private val gameAreaBottomMargin = 100f
    private val gameAreaWidth = 900f
    private val gameAreaHeight = 1410f - gameAreaTopMargin - gameAreaBottomMargin
    private var spawnPoint = Offset(0f, -gameAreaHeight / 2 + 150f)

    private fun setSpawnPoint(x: Float, y: Float) {
        spawnPoint = Offset(x, y)
    }

    private val borderObstacles = listOf(
        Obstacle(-gameAreaWidth / 2, -gameAreaHeight / 2, gameAreaWidth, 20f),
        Obstacle(-gameAreaWidth / 2, gameAreaHeight / 2 - 20f, gameAreaWidth, 20f),
        Obstacle(-gameAreaWidth / 2, -gameAreaHeight / 2, 20f, gameAreaHeight),
        Obstacle(gameAreaWidth / 2 - 20f, -gameAreaHeight / 2, 20f, gameAreaHeight)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        loadLevel()

        setContent {
            val tiempoRestante = remember { mutableStateOf(60) }

            // Controlamos el estado del cronómetro
            LaunchedEffect(gameState) {
                if (gameState != GameState.PLAYING) {
                    tiempoRestante.value = 60 // Resetear tiempo si reiniciamos
                }
            }

            MazeGame(
                x = _x,
                y = _y,
                items = items,
                gameState = gameState,
                tiempoRestante = tiempoRestante,
                onTimeOut = { gameState = GameState.GAME_OVER },
                onRestart = {
                    setSpawnPoint(0f, -gameAreaHeight / 2 + 150f)
                    loadLevel()
                    gameState = GameState.PLAYING
                    tiempoRestante.value = 60
                },
                borderObstacles = borderObstacles
            )
        }
    }

    private fun loadLevel() {
        val ballRadius = 20f
        val allObstacles = borderObstacles + obstacles

        var startX = spawnPoint.x
        var startY = spawnPoint.y

        // Evita colocar la pelota dentro de un obstáculo
        var safeSpawnFound = false
        for (i in 0..100) { // Reintenta hasta 100 veces
            if (!checkCollision(startX, startY, ballRadius, allObstacles)) {
                safeSpawnFound = true
                break
            }
            startY -= 5f // Mueve hacia arriba
        }

        if (!safeSpawnFound) {
            // Si no encuentra espacio seguro, usa el valor original y que colisione
            println("⚠️ No se encontró un punto de aparición seguro.")
        }

        _x = startX
        _y = startY

        velocityX = 0f
        velocityY = 0f

        items.clear()
        items.addAll(
            listOf(
                Item(-100f, -200f),
                Item(150f, 300f),
                Item(0f, 400f),
                Item(200f, 550f)
            )
        )
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
        if (gameState != GameState.PLAYING) return

        event?.let {
            val ax = it.values[0]
            val ay = it.values[1]

            velocityX -= ax * 1.2f
            velocityY += ay * 1.2f

            velocityX = max(-8f, min(8f, velocityX))
            velocityY = max(-8f, min(8f, velocityY))

            velocityX *= 0.92f
            velocityY *= 0.92f

            handleMovementWithCollision()
            checkItemCollection()

            if (items.all { it.collected }) {
                gameState = GameState.LEVEL_COMPLETE
            }
        }
    }

    private fun handleMovementWithCollision() {
        val ballRadius = 16f
        val allObstacles = borderObstacles + obstacles

        // Movimiento en X
        var newX = _x + velocityX
        if (checkCollision(newX, _y, ballRadius, allObstacles)) {
            while (!checkCollision(_x + sign(velocityX), _y, ballRadius, allObstacles)) {
                _x += sign(velocityX)
            }
            velocityX = -velocityX * 0.6f // rebote suave
        } else {
            _x = newX
        }

        // Movimiento en Y
        var newY = _y + velocityY
        if (checkCollision(_x, newY, ballRadius, allObstacles)) {
            while (!checkCollision(_x, _y + sign(velocityY), ballRadius, allObstacles)) {
                _y += sign(velocityY)
            }
            velocityY = -velocityY * 0.6f
        } else {
            _y = newY
        }
    }

    private fun sign(value: Float): Float {
        return when {
            value > 0 -> 1f
            value < 0 -> -1f
            else -> 0f
        }
    }


    private fun checkCollision(x: Float, y: Float, radius: Float, obstacles: List<Obstacle>): Boolean {
        return obstacles.any { obstacle ->
            // Encuentra el punto más cercano del obstáculo al centro del círculo
            val closestX = x.coerceIn(obstacle.x, obstacle.x + obstacle.width)
            val closestY = y.coerceIn(obstacle.y, obstacle.y + obstacle.height)

            // Calcula la distancia desde el centro del círculo al punto más cercano
            val dx = x - closestX
            val dy = y - closestY

            // Si esa distancia es menor al radio, hay colisión
            dx * dx + dy * dy < radius * radius
        }
    }
    private fun checkItemCollection() {
        val center = Offset(_x, _y)
        items.forEach {
            if (!it.collected && (Offset(it.x, it.y) - center).getDistance() < 20f) {
                it.collected = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

enum class GameState { PLAYING, LEVEL_COMPLETE, GAME_OVER }

data class Obstacle(val x: Float, val y: Float, val width: Float, val height: Float)
data class Item(val x: Float, val y: Float, val radius: Float = 10f, var collected: Boolean = false)

val obstacles = listOf(
    //(origen_X, origen_Y, ancho, largo)
    //1
    Obstacle(-350f, -450f, 200f, 40f),//-
    Obstacle(-150f,-450f,40f, 125f),//|
    Obstacle(-350f,-450f,40f,251f),//|
    Obstacle(-350f,-200f,500f,40f),//-

    Obstacle(-50f, -450f, 350f, 40f),//-
    Obstacle(-50f,-450f,40f, 100f),//|
    Obstacle(-50f,-351f,175f,40f),//-
    Obstacle(300f,-450f,40f,350f),//|
    Obstacle(300f,-101f,150f,40f),//-

    Obstacle(150f, -200f, 40f, 200f),//|
    Obstacle(150f,-1f,300f, 40f),//-
    Obstacle(350f,100f,40f,200f),//|
    Obstacle(-200f,0f,200f,40f),//-

    Obstacle(0f, 0f, 40f, 240f),//|
    Obstacle(-350f,200f,350f, 40f),//-
    Obstacle(-200f,239f,150f,41f),//-
    Obstacle(-275f,275f,40f,125f),//|
    Obstacle(-200f,350f,150f,40f),//-
    Obstacle(-50f,350f,40f,80f),//|

    Obstacle(-350f,-100f,350f, 40f),//-
    Obstacle(-350f,-100f,40f,200f),//|
    Obstacle(-350f,100f,250f,40f),//-


)

@Composable
fun MazeGame(
    x: Float,
    y: Float,
    items: List<Item>,
    gameState: GameState,
    tiempoRestante: MutableState<Int>,
    onTimeOut: () -> Unit,
    onRestart: () -> Unit,
    borderObstacles: List<Obstacle>
) {
    val topMargin = 80f
    val bottomMargin = 60f

    val LOGICAL_WIDTH = 900f
    val LOGICAL_HEIGHT = 1230f // 1410 - topMargin - bottomMargin

    Box(modifier = Modifier.fillMaxSize()) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF2E3440), size = size)

            val canvasWidth = size.width
            val canvasHeight = size.height - topMargin.dp.toPx() - bottomMargin.dp.toPx()

            val scaleX = canvasWidth / LOGICAL_WIDTH
            val scaleY = canvasHeight / LOGICAL_HEIGHT
            val scale = min(scaleX, scaleY)

            val originX = size.width / 2
            val originY = topMargin.dp.toPx() + canvasHeight / 2

            val allObstacles = borderObstacles + obstacles

            // Obstáculos
            allObstacles.forEach {
                drawRect(
                    color = Color(0xFF5E81AC),
                    topLeft = Offset(originX + it.x * scale, originY + it.y * scale),
                    size = Size(it.width * scale, it.height * scale)
                )
            }

            // Ítems
            items.forEach {
                if (!it.collected) {
                    drawCircle(
                        color = Color(0xFFA3BE8C),
                        radius = it.radius * scale,
                        center = Offset(originX + it.x * scale, originY + it.y * scale)
                    )
                }
            }

            // Pelota
            drawCircle(
                color = Color(0xFFBF616A),
                radius = 16f * scale, // radio lógico escalado
                center = Offset(originX + x * scale, originY + y * scale)
            )
        }

        // Cronómetro
        Cronometro(
            tiempoRestante = tiempoRestante,
            isActive = gameState == GameState.PLAYING,
            onTimeOut = onTimeOut,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        // Publicidad
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.LightGray)
                .fillMaxWidth()
                .height(bottomMargin.dp)
        ) {
            Text(
                "Área de Publicidad",
                modifier = Modifier.align(Alignment.Center),
                color = Color.DarkGray
            )
        }

        // Conteo de ítems
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text("Objetos: ${items.count { it.collected }}/${items.size}", color = Color.White)
        }

        // Mensajes de estado
        when (gameState) {
            GameState.LEVEL_COMPLETE -> OverlayMessage("\u00a1Nivel Completado!", onRestart)
            GameState.GAME_OVER -> OverlayMessage("\u00a1Tiempo terminado!", onRestart)
            else -> {}
        }
    }
}
