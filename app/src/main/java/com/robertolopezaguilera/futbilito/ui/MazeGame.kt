package com.robertolopezaguilera.futbilito.ui

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.robertolopezaguilera.futbilito.GameActivity
import com.robertolopezaguilera.futbilito.GameEngine
import com.robertolopezaguilera.futbilito.GameObstacle
import com.robertolopezaguilera.futbilito.GameState
import com.robertolopezaguilera.futbilito.MainActivity
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.Item
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.toGameObstacle
import kotlin.math.max

@Composable
fun MazeGame(
    nivel: Nivel?,
    itemsFromDb: List<Item>,
    borderObstacles: List<Obstaculo>,
    obstaclesFromDb: List<Obstaculo>,
    tiempoRestante: MutableState<Int>,
    onTimeOut: () -> Unit,
    onRestart: () -> Unit,
    tiltX: Float,
    tiltY: Float,
    onLevelScored: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coinSound = remember { MediaPlayer.create(context, R.raw.coin_sound) }
    val coinPainter = painterResource(id = R.drawable.ic_coin)

    // Tiempo inicial del nivel (evita dividir entre 0)
    val tiempoInicial = nivel?.tiempo ?: 60

    // Estado local para evitar guardar varias veces
    var puntuacionLocal by remember { mutableStateOf<Int?>(null) }

    // Motor de juego
    val gameEngine = remember(itemsFromDb, obstaclesFromDb, borderObstacles, nivel) {
        GameEngine(
            borderObstacles = borderObstacles.map { it.toGameObstacle() },
            obstacles = obstaclesFromDb.map { it.toGameObstacle() },
            itemsFromDb = itemsFromDb,
            spawnPoint = Offset(0f, 0f)
        ) {
            coinSound.start()
        }
    }

    // Movimiento con sensores
    LaunchedEffect(tiltX, tiltY) {
        gameEngine.updateWithSensor(tiltX, tiltY)
    }

    // Calcular puntuación al recolectar todos los items
    val collectedCount = gameEngine.items.count { it.collected }
    LaunchedEffect(collectedCount) {
        val total = gameEngine.items.size
        if (total > 0 && collectedCount == total && puntuacionLocal == null) {
            val porcentajeRestante = (tiempoRestante.value.toFloat() / tiempoInicial.toFloat()) * 100f
            val score = when {
                porcentajeRestante >= 30f -> 4
                porcentajeRestante >= 20f -> 3
                porcentajeRestante >= 10f -> 2
                else -> 1
            }
            val puntuacionActualNivel = nivel?.puntuacion ?: 0
            if (score > puntuacionActualNivel) {
                puntuacionLocal = score
                onLevelScored(score) // persistir mejor puntuación
            } else {
                puntuacionLocal = puntuacionActualNivel
            }
            gameEngine.gameState = GameState.LEVEL_COMPLETE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF2E3440), size = size)
            val originX = size.width / 2f
            val originY = size.height / 2f

            // Bordes
            gameEngine.borderObstacles.forEach { o ->
                drawRect(
                    color = Color(0xFF5E81AC),
                    topLeft = Offset(originX + o.x, originY + o.y),
                    size = Size(o.width, o.height)
                )
            }

            // Obstáculos
            gameEngine.obstacles.forEach { o ->
                drawRect(
                    color = Color(0xFF5E81AC),
                    topLeft = Offset(originX + o.x, originY + o.y),
                    size = Size(o.width, o.height)
                )
            }

            // Items (solo si no han sido recolectados)
            gameEngine.items.forEach { item ->
                if (!item.collected) {
                    val itemSize = item.radius * 2
                    translate(
                        left = originX + item.x - item.radius,
                        top = originY + item.y - item.radius
                    ) {
                        with(coinPainter) { draw(size = Size(itemSize, itemSize)) }
                    }
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
            onTimeOut = {
                gameEngine.gameState = GameState.GAME_OVER
                onTimeOut()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        // Overlay info (x/total)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pig),
                    contentDescription = "Monedas",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${collectedCount} / ${gameEngine.items.size}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "nivel= ${nivel?.id}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlays - CORREGIDO: Usando las nuevas funciones específicas
        when (gameEngine.gameState) {
            GameState.LEVEL_COMPLETE -> {
                val scoreShown = puntuacionLocal ?: 0
                LevelCompletedOverlay(
                    starsEarned = scoreShown,
                    onRestart = onRestart,
                    onGoToNiveles = {
                        // Vuelve a la lista de niveles (estaba debajo en el back stack)
                        activity?.finish()
                    },
                    onGoToCategorias = {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra("startDestination", "categorias")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                        activity?.finish()
                    },
                    onNextLevel = {
                        val nextNivelId = (nivel?.id ?: 0) + 1
                        val intent = Intent(context, GameActivity::class.java).apply {
                            putExtra("nivelId", nextNivelId)
                        }
                        context.startActivity(intent)
                        activity?.finish()
                    },
                    message = ""
                )
            }

            GameState.GAME_OVER -> {
                TimeUpOverlay(
                    message = "¡Tiempo terminado!",
                    onRestart = onRestart,
                    onGoToNiveles = {
                        activity?.finish()
                    },
                    onGoToCategorias = {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra("startDestination", "categorias")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                        activity?.finish()
                    },
                    onNextLevel = {
                        val nextNivelId = (nivel?.id ?: 0) + 1
                        val intent = Intent(context, GameActivity::class.java).apply {
                            putExtra("nivelId", nextNivelId)
                        }
                        context.startActivity(intent)
                        activity?.finish()
                    }
                )
            }

            else -> {}
        }
    }
}