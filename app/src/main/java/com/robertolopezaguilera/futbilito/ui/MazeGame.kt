package com.robertolopezaguilera.futbilito.ui

import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.GameEngine
import com.robertolopezaguilera.futbilito.GameObstacle
import com.robertolopezaguilera.futbilito.GameState
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.toGameObstacle

@Composable
fun MazeGame(
    nivel: Nivel?,
    itemsFromDb: List<com.robertolopezaguilera.futbilito.data.Item>,
    obstaclesFromDb: List<com.robertolopezaguilera.futbilito.data.Obstaculo>,
    tiempoRestante: MutableState<Int>,
    onTimeOut: () -> Unit,
    onRestart: () -> Unit,
    tiltX: Float,   // Recibe tiltX desde MainActivity
    tiltY: Float    // Recibe tiltY desde MainActivity
) {
    val context = LocalContext.current
    val coinSound = remember { MediaPlayer.create(context, R.raw.coin_sound) }
    val coinPainter = painterResource(id = R.drawable.ic_coin)

    val gameEngine = remember(itemsFromDb, obstaclesFromDb, nivel) {
        GameEngine(
            borderObstacles = emptyList(),
            obstacles = obstaclesFromDb,
            itemsFromDb = itemsFromDb,
            spawnPoint = Offset(0f, 0f)
        ) {
            coinSound.start()
        }
    }

    // Actualizamos la posición de la pelota cada frame con tiltX/Y
    LaunchedEffect(tiltX, tiltY) {
        gameEngine.updateWithSensor(tiltX, tiltY)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF2E3440), size = size)

            val originX = size.width / 2f
            val originY = size.height / 2f

            // Obstáculos
            (obstaclesFromDb.map { it.toGameObstacle() } +
                    gameEngine.items.map { GameObstacle(it.x - it.radius, it.y - it.radius, it.radius*2, it.radius*2) })
                .forEach { o ->
                    drawRect(
                        color = Color(0xFF5E81AC),
                        topLeft = Offset(originX + o.x, originY + o.y),
                        size = Size(o.width, o.height)
                    )
                }

            // Items
            gameEngine.items.forEach { item ->
                val itemSize = item.radius * 2
                translate(
                    left = originX + item.x - item.radius,
                    top = originY + item.y - item.radius
                ) {
                    with(coinPainter) { draw(size = Size(itemSize, itemSize)) }
                }
            }

            // Pelota
            drawCircle(
                color = Color(0xFFBF616A),
                radius = 16f,
                center = Offset(originX + gameEngine.x, originY + gameEngine.y)
            )
        }

        // Cronómetro
        Cronometro(
            tiempoRestante = tiempoRestante,
            isActive = gameEngine.gameState == GameState.PLAYING,
            onTimeOut = onTimeOut,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        )

        // Overlay de información
        Box(
            modifier = Modifier.align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painter = painterResource(id = R.drawable.ic_pig), contentDescription = "Monedas", modifier = Modifier.size(24.dp))
                Text(text = "${gameEngine.items.count { it.collected }}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "nivel= ${nivel?.id}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Overlays de fin de juego
        when (gameEngine.gameState) {
            GameState.LEVEL_COMPLETE -> OverlayMessage("¡Nivel Completado!", onRestart)
            GameState.GAME_OVER -> OverlayMessage("¡Tiempo terminado!", onRestart)
            else -> {}
        }
    }
}
