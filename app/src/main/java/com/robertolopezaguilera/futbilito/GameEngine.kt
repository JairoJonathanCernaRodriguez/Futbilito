package com.robertolopezaguilera.futbilito

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.data.Item
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// 👇 Nuevo enum para tipos de poder
enum class PowerType {
    SPEED_BOOST, GHOST_MODE, NONE
}

class GameEngine(
    val borderObstacles: List<GameObstacle>,
    val obstacles: List<GameObstacle>,
    private val itemsFromDb: List<Item>,
    spawnPoint: Offset,
    private val onCoinCollected: (() -> Unit)? = null,
    private val onPowerActivated: ((PowerType) -> Unit)? = null, // 👈 Nuevo callback
) {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    private var velocityX by mutableStateOf(0f)
    private var velocityY by mutableStateOf(0f)
    var gameState by mutableStateOf(GameState.PLAYING)

    val items = mutableStateListOf<GameItem>()
    private var spawnPoint = spawnPoint

    // 👇 Nuevas variables para optimización
    private var lastUpdateTime by mutableStateOf(0L)
    private val targetFrameTime = 16L // ~60 FPS
    private val allObstacles = borderObstacles + obstacles // 👈 Pre-calcular una vez

    // 👇 Sistema de poderes mejorado
    var isPaused by mutableStateOf(false)
    var activePower by mutableStateOf(PowerType.NONE)
    private var powerTimer by mutableStateOf(0f)

    // 👇 Duración de los poderes (en segundos)
    private val speedBoostDuration = 25f
    private val ghostModeDuration = 15f

    init { loadLevel() }

    fun setSpawnPoint(x: Float, y: Float) { spawnPoint = Offset(x, y) }

    fun loadLevel() {
        val ballRadius = 20f

        // 👇 Buscar una posición segura para spawn
        val safeSpawnPoint = findSafeSpawnPoint(ballRadius)

        // 👇 Resetear estados
        gameState = GameState.PLAYING
        activePower = PowerType.NONE
        powerTimer = 0f
        isPaused = false

        x = safeSpawnPoint.x
        y = safeSpawnPoint.y
        velocityX = 0f
        velocityY = 0f

        items.clear()
        items.addAll(itemsFromDb.map { it.toGameItem() })

        // 👇 Resetear el tiempo de actualización
        lastUpdateTime = System.currentTimeMillis()
    }

    private fun findSafeSpawnPoint(ballRadius: Float): Offset {
        // Estrategia 1: Probar el spawn point original
        if (!checkCollision(spawnPoint.x, spawnPoint.y, ballRadius)) {
            return spawnPoint
        }

        // Estrategia 2: Buscar cerca de los items (generalmente están en áreas seguras)
        for (item in itemsFromDb.map { it.toGameItem() }) {
            if (!checkCollision(item.x, item.y, ballRadius)) {
                return Offset(item.x, item.y)
            }
        }

        // Estrategia 3: Búsqueda en espiral desde el spawn point
        val maxRadius = 300f
        val steps = 24

        var radius = 50f
        while (radius <= maxRadius) {
            for (i in 0 until steps) {
                val angle = 2 * PI * i / steps
                val testX = spawnPoint.x + radius * cos(angle).toFloat()
                val testY = spawnPoint.y + radius * sin(angle).toFloat()

                if (!checkCollision(testX, testY, ballRadius)) {
                    return Offset(testX, testY)
                }
            }
            radius += 30f
        }

        // Estrategia 4: Buscar en las esquinas del mapa
        val cornerPoints = listOf(
            Offset(-450f, -450f), // Esquina superior izquierda
            Offset(450f, -450f),  // Esquina superior derecha
            Offset(-450f, 450f),  // Esquina inferior izquierda
            Offset(450f, 450f)    // Esquina inferior derecha
        )

        for (point in cornerPoints) {
            if (!checkCollision(point.x, point.y, ballRadius)) {
                return point
            }
        }

        // Último recurso: usar una posición por defecto lejos del centro
        println("⚠️ No se encontró posición segura, usando posición por defecto")
        return Offset(100f, 100f)
    }

    fun updateWithSensor(ax: Float, ay: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < targetFrameTime) {
            return // 👈 Saltar frame para mantener 60 FPS
        }
        lastUpdateTime = currentTime

        if (gameState != GameState.PLAYING || isPaused) return

        // 👇 Actualizar temporizador del poder activo
        updatePowerTimer()

        // 👇 Aplicar efectos del poder activo
        val accelerationFactor = when (activePower) {
            PowerType.SPEED_BOOST -> 0.7f
            else -> 0.5f
        }

        val maxSpeed = when (activePower) {
            PowerType.SPEED_BOOST -> 35f
            else -> 20f
        }

        velocityX += ax * accelerationFactor
        velocityY += ay * accelerationFactor

        velocityX = max(-maxSpeed, min(maxSpeed, velocityX))
        velocityY = max(-maxSpeed, min(maxSpeed, velocityY))

        handleMovementWithCollision()
        checkItemCollection()

        if (items.all { it.collected }) gameState = GameState.LEVEL_COMPLETE
    }

    // 👇 Nueva función para actualizar el temporizador de poderes
    private fun updatePowerTimer() {
        if (activePower != PowerType.NONE) {
            powerTimer -= 0.016f // Asumiendo ~60 FPS
            if (powerTimer <= 0f) {
                deactivatePower()
            }
        }
    }

    // 👇 Nueva función para desactivar el poder actual
    private fun deactivatePower() {
        activePower = PowerType.NONE
        powerTimer = 0f
    }

    fun pause() {
        isPaused = true
        velocityX = 0f
        velocityY = 0f
    }

    fun resume() {
        isPaused = false
    }

    // 👇 Funciones para activar poderes
    fun activateSpeedBoost() {
        activePower = PowerType.SPEED_BOOST
        powerTimer = speedBoostDuration
        onPowerActivated?.invoke(PowerType.SPEED_BOOST)
    }

    fun activateGhostMode() {
        activePower = PowerType.GHOST_MODE
        powerTimer = ghostModeDuration
        onPowerActivated?.invoke(PowerType.GHOST_MODE)
    }

    private fun handleMovementWithCollision() {
        val ballRadius = 16f

        var newX = x + velocityX
        var newY = y + velocityY

        // 👇 Solo verificar colisiones si NO está en modo fantasma
        if (activePower != PowerType.GHOST_MODE) {
            // --- COLISIONES CON OBSTÁCULOS ---
            var collided = false

            // Mover en X
            if (!checkCollision(newX, y, ballRadius)) {
                x = newX
            } else {
                velocityX = -velocityX * 0.8f
                collided = true
            }

            // Mover en Y
            if (!checkCollision(x, newY, ballRadius)) {
                y = newY
            } else {
                velocityY = -velocityY * 0.8f
                collided = true
            }

            if (collided) {
                x += velocityX * 0.1f
                y += velocityY * 0.1f
            }
        } else {
            // 👇 Modo fantasma: movimiento sin colisiones
            x = newX
            y = newY
        }

        // Fricción (aplicar siempre)
        velocityX *= 0.90f
        velocityY *= 0.90f
    }

    // 👇 Optimizada para usar allObstacles pre-calculado
    private fun checkCollision(x: Float, y: Float, radius: Float): Boolean {
        for (obstacle in allObstacles) {
            val closestX = x.coerceIn(obstacle.x, obstacle.x + obstacle.width)
            val closestY = y.coerceIn(obstacle.y, obstacle.y + obstacle.height)
            val dx = x - closestX
            val dy = y - closestY
            if (dx * dx + dy * dy < radius * radius) {
                return true
            }
        }
        return false
    }

    private fun checkItemCollection() {
        val ballRadius = 16f
        val center = Offset(x, y)

        for (item in items) {
            if (!item.collected) {
                val dx = item.x - center.x
                val dy = item.y - center.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < ballRadius + item.radius) {
                    item.collected = true
                    println("🎯 Item recolectado en GameEngine")
                    onCoinCollected?.invoke() // 👈 Esto debe llamarse
                }
            }
        }
    }

    // 👇 Nueva función para obtener información del poder activo
    fun getActivePowerInfo(): PowerInfo {
        return PowerInfo(
            type = activePower,
            remainingTime = powerTimer,
            isActive = activePower != PowerType.NONE
        )
    }
}

// 👇 Nueva data class para información del poder
data class PowerInfo(
    val type: PowerType,
    val remainingTime: Float,
    val isActive: Boolean
)

data class GameObstacle(val x: Float, val y: Float, val width: Float, val height: Float)
data class GameItem(val x: Float, val y: Float, val radius: Float = 40f, var collected: Boolean = false)

// 🔹 Extensiones para transformar DB → objetos del juego
fun com.robertolopezaguilera.futbilito.data.Obstaculo.toGameObstacle() =
    GameObstacle(coordenadaX.toFloat(), coordenadaY.toFloat(), ancho.toFloat(), largo.toFloat())

fun Item.toGameItem() =
    GameItem(coordenadaX.toFloat(), coordenadaY.toFloat())