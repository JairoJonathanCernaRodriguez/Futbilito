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

class GameEngine(
    val borderObstacles: List<GameObstacle>,   // 👈 ya todos GameObstacle
    val obstacles: List<GameObstacle>,         // 👈 convertidos antes de entrar
    private val itemsFromDb: List<Item>,
    spawnPoint: Offset,
    private val onCoinCollected: (() -> Unit)? = null,
) {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    private var velocityX by mutableStateOf(0f)
    private var velocityY by mutableStateOf(0f)
    var gameState by mutableStateOf(GameState.PLAYING)

    val items = mutableStateListOf<GameItem>()
    private var spawnPoint = spawnPoint

    init { loadLevel() }

    fun setSpawnPoint(x: Float, y: Float) { spawnPoint = Offset(x, y) }

    fun loadLevel() {
        val ballRadius = 20f
        val allObstacles = borderObstacles + obstacles

        var startX = spawnPoint.x
        var startY = spawnPoint.y
        var safeSpawnFound = false

        for (i in 0..100) {
            if (!checkCollision(startX, startY, ballRadius, allObstacles)) {
                safeSpawnFound = true
                break
            }
            startY -= 5f
        }
        if (!safeSpawnFound) println("⚠️ No se encontró un punto de aparición seguro.")

        x = startX
        y = startY
        velocityX = 0f
        velocityY = 0f

        items.clear()
        items.addAll(itemsFromDb.map { it.toGameItem() })
    }

    fun updateWithSensor(ax: Float, ay: Float) {
        if (gameState != GameState.PLAYING) return

        val accelerationFactor = 0.5f
        val maxSpeed = 20f

        velocityX += ax * accelerationFactor
        velocityY += ay * accelerationFactor

        velocityX = max(-maxSpeed, min(maxSpeed, velocityX))
        velocityY = max(-maxSpeed, min(maxSpeed, velocityY))

        handleMovementWithCollision()
        checkItemCollection()

        if (items.all { it.collected }) gameState = GameState.LEVEL_COMPLETE
    }

    private fun handleMovementWithCollision() {
        val ballRadius = 16f
        val allObstacles = borderObstacles + obstacles

        var newX = x + velocityX
        var newY = y + velocityY

        // --- COLISIONES CON OBSTÁCULOS ---
        var collided = false

        // Mover en X
        if (!checkCollision(newX, y, ballRadius, allObstacles)) {
            x = newX
        } else {
            velocityX = -velocityX * 0.8f
            collided = true
        }

        // Mover en Y
        if (!checkCollision(x, newY, ballRadius, allObstacles)) {
            y = newY
        } else {
            velocityY = -velocityY * 0.8f
            collided = true
        }

        if (collided) {
            x += velocityX * 0.1f
            y += velocityY * 0.1f
        }

        // Fricción
        velocityX *= 0.90f
        velocityY *= 0.90f
    }

    private fun checkCollision(x: Float, y: Float, radius: Float, obstacles: List<GameObstacle>): Boolean {
        return obstacles.any { obstacle ->
            val closestX = x.coerceIn(obstacle.x, obstacle.x + obstacle.width)
            val closestY = y.coerceIn(obstacle.y, obstacle.y + obstacle.height)
            val dx = x - closestX
            val dy = y - closestY
            dx * dx + dy * dy < radius * radius
        }
    }

    private fun checkItemCollection() {
        val ballRadius = 16f
        val center = Offset(x, y)
        items.forEach { item ->
            if (!item.collected) {
                val distance = (Offset(item.x, item.y) - center).getDistance()
                if (distance < ballRadius + item.radius) {
                    item.collected = true
                    onCoinCollected?.invoke()
                }
            }
        }
    }
}

data class GameObstacle(val x: Float, val y: Float, val width: Float, val height: Float)
data class GameItem(val x: Float, val y: Float, val radius: Float = 40f, var collected: Boolean = false)

// 🔹 Extensiones para transformar DB → objetos del juego
fun com.robertolopezaguilera.futbilito.data.Obstaculo.toGameObstacle() =
    GameObstacle(coordenadaX.toFloat(), coordenadaY.toFloat(), ancho.toFloat(), largo.toFloat())

fun Item.toGameItem() =
    GameItem(coordenadaX.toFloat(), coordenadaY.toFloat())
