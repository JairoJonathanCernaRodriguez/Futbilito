package com.robertolopezaguilera.futbilito

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.robertolopezaguilera.futbilito.data.Item
import com.robertolopezaguilera.futbilito.data.Powers
import kotlin.math.*

enum class PowerType {
    SPEED_BOOST, GHOST_MODE, NONE
}

class GameEngine(
    val borderObstacles: List<GameObstacle>,
    val obstacles: List<GameObstacle>,
    private val itemsFromDb: List<Item>,
    private val powersFromDb: List<Powers>,
    spawnPoint: Offset,
    private val onCoinCollected: (() -> Unit)? = null,
    private val onPowerCollected: ((PowerType) -> Unit)? = null,
) {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    private var velocityX by mutableStateOf(0f)
    private var velocityY by mutableStateOf(0f)
    var gameState by mutableStateOf(GameState.PLAYING)

    val items = mutableStateListOf<GameItem>()
    val powers = mutableStateListOf<GamePower>()

    private companion object {
        const val BASE_SPEED = 15f
        const val BOOSTED_SPEED = 25f
        const val ACCELERATION_SENSITIVITY = 0.8f
        const val FRICTION = 0.92f
        const val BOUNCE_DAMPING = 0.7f
    }

    private var spawnPoint = spawnPoint
    private var lastUpdateTime by mutableStateOf(0L)
    private val targetFrameTime = 16L

    // 👇 ELIMINAR esta línea - NO combinar obstáculos
    // private val allObstacles = borderObstacles + obstacles // ❌ QUITAR

    var isPaused by mutableStateOf(false)
    var activePower by mutableStateOf(PowerType.NONE)
    private var powerTimer by mutableStateOf(0f)

    private val speedBoostDuration = 10f
    private val ghostModeDuration = 8f

    private val currentMaxSpeed: Float
        get() = when (activePower) {
            PowerType.SPEED_BOOST -> BOOSTED_SPEED
            else -> BASE_SPEED
        }

    init {
        loadLevel()
        println("🎮 GameEngine iniciado con ${powersFromDb.size} poderes disponibles")
    }

    fun setSpawnPoint(x: Float, y: Float) { spawnPoint = Offset(x, y) }

    fun loadLevel() {
        val ballRadius = 20f
        val safeSpawnPoint = findSafeSpawnPoint(ballRadius)

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

        powers.clear()
        powers.addAll(powersFromDb.map { it.toGamePower() })

        lastUpdateTime = System.currentTimeMillis()
        println("🎮 Nivel cargado: ${items.size} items, ${powers.size} poderes")
    }

    fun updateWithSensor(ax: Float, ay: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < targetFrameTime) {
            return
        }
        lastUpdateTime = currentTime

        if (gameState != GameState.PLAYING || isPaused) return

        updatePowerTimer()

        velocityX += ax * ACCELERATION_SENSITIVITY
        velocityY += ay * ACCELERATION_SENSITIVITY

        val currentSpeed = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (currentSpeed > currentMaxSpeed) {
            val ratio = currentMaxSpeed / currentSpeed
            velocityX *= ratio
            velocityY *= ratio
        }

        handleMovementWithCollision()
        checkItemCollection()
        checkPowerCollection()

        if (items.all { it.collected }) {
            gameState = GameState.LEVEL_COMPLETE
            println("🎉 Nivel completado!")
        }
    }

    private fun updatePowerTimer() {
        if (activePower != PowerType.NONE) {
            powerTimer -= 0.016f
            if (powerTimer <= 0f) {
                println("⏰ Power-up desactivado: $activePower")
                deactivatePower()
            }
        }
    }

    private fun deactivatePower() {
        activePower = PowerType.NONE
        powerTimer = 0f
        val currentSpeed = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (currentSpeed > BASE_SPEED) {
            val ratio = BASE_SPEED / currentSpeed
            velocityX *= ratio
            velocityY *= ratio
        }
    }

    private fun handleMovementWithCollision() {
        val ballRadius = 16f
        var newX = x + velocityX
        var newY = y + velocityY

        // 👇 CORREGIDO: En modo fantasma, solo verificar colisión con bordes
        if (activePower == PowerType.GHOST_MODE) {
            // Ghost mode: ignorar obstáculos normales, pero verificar bordes
            handleMovementGhostMode(newX, newY, ballRadius)
        } else {
            // Modo normal: verificar ambos tipos de colisiones
            handleMovementNormal(newX, newY, ballRadius)
        }

        // Aplicar fricción
        velocityX *= FRICTION
        velocityY *= FRICTION

        // Detener movimiento si es muy lento
        if (abs(velocityX) < 0.1f) velocityX = 0f
        if (abs(velocityY) < 0.1f) velocityY = 0f
    }

    // 👇 NUEVA FUNCIÓN: Movimiento en modo fantasma (solo bordes)
    private fun handleMovementGhostMode(newX: Float, newY: Float, ballRadius: Float) {
        var collidedX = false
        var collidedY = false

        // Verificar colisión en X solo con bordes
        if (!checkBorderCollision(newX, y, ballRadius)) {
            x = newX
        } else {
            velocityX = -velocityX * BOUNCE_DAMPING
            collidedX = true
        }

        // Verificar colisión en Y solo con bordes
        if (!checkBorderCollision(x, newY, ballRadius)) {
            y = newY
        } else {
            velocityY = -velocityY * BOUNCE_DAMPING
            collidedY = true
        }

        // Pequeño ajuste después de colisión
        if (collidedX || collidedY) {
            x += velocityX * 0.05f
            y += velocityY * 0.05f
        }
    }

    // 👇 NUEVA FUNCIÓN: Movimiento normal (bordes + obstáculos)
    private fun handleMovementNormal(newX: Float, newY: Float, ballRadius: Float) {
        var collidedX = false
        var collidedY = false

        // Verificar colisión en X con ambos tipos
        if (!checkCollision(newX, y, ballRadius)) {
            x = newX
        } else {
            velocityX = -velocityX * BOUNCE_DAMPING
            collidedX = true
        }

        // Verificar colisión en Y con ambos tipos
        if (!checkCollision(x, newY, ballRadius)) {
            y = newY
        } else {
            velocityY = -velocityY * BOUNCE_DAMPING
            collidedY = true
        }

        // Pequeño ajuste después de colisión
        if (collidedX || collidedY) {
            x += velocityX * 0.05f
            y += velocityY * 0.05f
        }
    }

    // 👇 NUEVA FUNCIÓN: Verificar colisión solo con bordes
    private fun checkBorderCollision(x: Float, y: Float, radius: Float): Boolean {
        for (border in borderObstacles) {
            val closestX = x.coerceIn(border.x, border.x + border.width)
            val closestY = y.coerceIn(border.y, border.y + border.height)
            val dx = x - closestX
            val dy = y - closestY
            if (dx * dx + dy * dy < radius * radius) {
                return true
            }
        }
        return false
    }

    // 👇 NUEVA FUNCIÓN: Verificar colisión solo con obstáculos normales
    private fun checkNormalObstacleCollision(x: Float, y: Float, radius: Float): Boolean {
        for (obstacle in obstacles) {
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

    // 👇 FUNCIÓN ORIGINAL: Verificar colisión con ambos tipos (para modo normal)
    private fun checkCollision(x: Float, y: Float, radius: Float): Boolean {
        // Verificar primero bordes (más importante)
        if (checkBorderCollision(x, y, radius)) {
            return true
        }
        // Luego verificar obstáculos normales
        if (checkNormalObstacleCollision(x, y, radius)) {
            return true
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
                    println("🎯 Item recolectado - Distancia: $distance")
                    onCoinCollected?.invoke()
                    break
                }
            }
        }
    }

    private fun checkPowerCollection() {
        val ballRadius = 16f
        val center = Offset(x, y)

        for (power in powers) {
            if (!power.collected) {
                val dx = power.x - center.x
                val dy = power.y - center.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < ballRadius + power.radius) {
                    power.collected = true
                    println("⚡ Power-up recolectado: ${power.type} - Distancia: $distance")

                    when (power.type) {
                        PowerType.SPEED_BOOST -> activateSpeedBoost()
                        PowerType.GHOST_MODE -> activateGhostMode()
                        else -> {}
                    }

                    onPowerCollected?.invoke(power.type)
                    break
                }
            }
        }
    }

    fun activateSpeedBoost() {
        if (activePower != PowerType.SPEED_BOOST) {
            activePower = PowerType.SPEED_BOOST
            powerTimer = speedBoostDuration
            println("🚀 Speed Boost activado por $speedBoostDuration segundos")
            onPowerCollected?.invoke(PowerType.SPEED_BOOST)
        }
    }

    fun activateGhostMode() {
        if (activePower != PowerType.GHOST_MODE) {
            activePower = PowerType.GHOST_MODE
            powerTimer = ghostModeDuration
            println("👻 Ghost Mode activado por $ghostModeDuration segundos")
            onPowerCollected?.invoke(PowerType.GHOST_MODE)
        }
    }

    fun pause() {
        isPaused = true
        velocityX = 0f
        velocityY = 0f
    }

    fun resume() {
        isPaused = false
    }

    fun getActivePowerInfo(): PowerInfo {
        return PowerInfo(
            type = activePower,
            remainingTime = powerTimer,
            isActive = activePower != PowerType.NONE
        )
    }

    fun getCurrentSpeed(): Float {
        return sqrt(velocityX * velocityX + velocityY * velocityY)
    }

    fun getMaxSpeed(): Float {
        return currentMaxSpeed
    }

    private fun findSafeSpawnPoint(ballRadius: Float): Offset {
        // Usar la función checkCollision original que verifica ambos tipos
        if (!checkCollision(spawnPoint.x, spawnPoint.y, ballRadius)) {
            return spawnPoint
        }

        for (item in itemsFromDb.map { it.toGameItem() }) {
            if (!checkCollision(item.x, item.y, ballRadius)) {
                return Offset(item.x, item.y)
            }
        }

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

        val cornerPoints = listOf(
            Offset(-450f, -450f),
            Offset(450f, -450f),
            Offset(-450f, 450f),
            Offset(450f, 450f)
        )

        for (point in cornerPoints) {
            if (!checkCollision(point.x, point.y, ballRadius)) {
                return point
            }
        }

        println("⚠️ No se encontró posición segura, usando posición por defecto")
        return Offset(100f, 100f)
    }
}

// 👇 Nueva data class para poderes del juego
data class GamePower(
    val x: Float,
    val y: Float,
    val type: PowerType,
    val radius: Float = 30f,
    var collected: Boolean = false
)

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

// 👇 Nueva extensión para transformar Powers
fun Powers.toGamePower(): GamePower {
    val powerType = when (this.tipo.lowercase()) {
        "speed_boost", "velocidad" -> PowerType.SPEED_BOOST
        "ghost_mode", "fantasma" -> PowerType.GHOST_MODE
        else -> PowerType.NONE
    }
    return GamePower(
        x = coordenadaX.toFloat(),
        y = coordenadaY.toFloat(),
        type = powerType
    )
}