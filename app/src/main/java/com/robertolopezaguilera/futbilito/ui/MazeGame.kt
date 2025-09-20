package com.robertolopezaguilera.futbilito.ui

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.GameActivity
import com.robertolopezaguilera.futbilito.GameEngine
import com.robertolopezaguilera.futbilito.GameState
import com.robertolopezaguilera.futbilito.MainActivity
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.admob.AdBanner
import com.robertolopezaguilera.futbilito.data.Item
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.toGameObstacle
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    onLevelScored: (Int) -> Unit = {},
    onAddCoins: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 👇 Estado de carga
    var isLoading by remember { mutableStateOf(true) }

    // 👇 Estado para mostrar animación de monedas
    var showCoinAnimation by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableStateOf(0) }

    // 👇 Estado para animación del cerdo cuando se recolecta un ítem
    var pigAnimationTrigger by remember { mutableStateOf(0) }

    // 👇 Gestión del MediaPlayer
    // 👇 REEMPLAZAR: MediaPlayer como remember mutableState
    var coinSound by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        coinSound = MediaPlayer.create(context, R.raw.coin_sound).apply {
            setOnCompletionListener {
                it.seekTo(0) // Resetear al completar
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            coinSound?.release()
            coinSound = null
        }
    }

    // Cargar recursos de forma asíncrona
    LaunchedEffect(nivel?.id) {
        isLoading = true
        // Pequeña pausa para permitir que la UI se renderice
        delay(100)
        isLoading = false
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    val coinPainter = painterResource(id = R.drawable.ic_coin)

    // Tiempo inicial del nivel
    val tiempoInicial = nivel?.tiempo ?: 60

    // Estado local para evitar guardar varias veces
    var puntuacionLocal by remember { mutableStateOf<Int?>(null) }

    // Estado para controlar si el juego está pausado
    var isGamePaused by remember { mutableStateOf(false) }

    // 👇 Optimizar el motor de juego con claves estables
    val gameEngine = remember(
        itemsFromDb.hashCode(),
        obstaclesFromDb.hashCode(),
        borderObstacles.hashCode(),
        nivel?.id
    ) {
        GameEngine(
            borderObstacles = borderObstacles.map { it.toGameObstacle() },
            obstacles = obstaclesFromDb.map { it.toGameObstacle() },
            itemsFromDb = itemsFromDb,
            spawnPoint = Offset(0f, 0f)
        ) {
            // 👇 CORREGIR: Reproducir sonido aquí directamente
            println("🟢 CALLBACK: Item recolectado")

            try {
                if (coinSound?.isPlaying == true) {
                    coinSound?.pause()
                    coinSound?.seekTo(0)
                }
                coinSound?.start()
                println("🔊 Sonido reproducido")
            } catch (e: Exception) {
                println("❌ Error reproduciendo sonido: ${e.message}")
                // Intentar recrear el MediaPlayer si falla
                coinSound = MediaPlayer.create(context, R.raw.coin_sound)
                coinSound?.start()
            }
        }
    }
    // 👇 Optimizar el sensor update
    LaunchedEffect(tiltX, tiltY) {
        if (!isGamePaused && gameEngine.gameState == GameState.PLAYING) {
            gameEngine.updateWithSensor(tiltX, tiltY)
        }
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

            // 👇 Calcular monedas ganadas según las estrellas
            val coins = when (score) {
                4 -> 15
                3 -> 10
                2 -> 8
                1 -> 5
                else -> 0
            }

            if (score > puntuacionActualNivel) {
                // 👇 Agregar monedas y mostrar animación
                onAddCoins(coins)
                coinsEarned = coins
                showCoinAnimation = true

                // 👇 Ocultar animación después de 3 segundos
                delay(3000)
                showCoinAnimation = false

                puntuacionLocal = score
                onLevelScored(score) // persistir mejor puntuación
            } else {
                puntuacionLocal = puntuacionActualNivel
            }
            gameEngine.gameState = GameState.LEVEL_COMPLETE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Contenedor principal del juego (ocupa todo el espacio disponible)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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

                // BOTÓN DE PAUSA - ARRIBA A LA DERECHA
                PauseButton(
                    isPaused = isGamePaused,
                    onPauseChange = { paused ->
                        isGamePaused = paused
                        if (paused) {
                            gameEngine.pause()
                        } else {
                            gameEngine.resume()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                // Cronómetro
                Cronometro(
                    tiempoRestante = tiempoRestante,
                    isActive = gameEngine.gameState == GameState.PLAYING,
                    isPaused = isGamePaused,
                    onTimeOut = {
                        gameEngine.gameState = GameState.GAME_OVER
                        onTimeOut() // ← Esto se ejecutará cuando el tiempo llegue a 0
                    },
                    onPauseChange = { paused ->
                        isGamePaused = paused
                        if (paused) {
                            gameEngine.pause()
                        } else {
                            gameEngine.resume()
                        }
                    },
                    isOnline = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Overlay info (x/total) con animación de cerdo
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
                        // 👇 Cerdo con animación cuando se recolecta un ítem
                        AnimatedPigIcon(trigger = pigAnimationTrigger)

                        Text(
                            text = "${collectedCount} / ${gameEngine.items.size}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 👇 Mostrar animación de monedas si está activa
                if (showCoinAnimation) {
                    CoinAnimation(
                        coins = coinsEarned,
                        modifier = Modifier.align(Alignment.Center)
                    )
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

            // 👇 Banner de anuncios en la parte inferior
            AdBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

// 👇 Composable CORREGIDO para animación del cerdo
@Composable
fun AnimatedPigIcon(trigger: Int) {
    // Animación cuando cambia el trigger
    val animationState by animateFloatAsState(
        targetValue = if (trigger > 0) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "pigAnimation"
    )

    // Calcular transformaciones basadas en el estado de animación
    val scale = 1f + animationState * 0.3f
    val rotation = animationState * 30f * sin(animationState * PI.toFloat() * 2)

    Image(
        painter = painterResource(id = R.drawable.ic_pig),
        contentDescription = "Monedas",
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
    )
}

// Nueva función de pantalla de carga
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E3440)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF5E81AC))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando nivel...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Nuevo composable para animación de monedas
@Composable
fun CoinAnimation(
    coins: Int,
    modifier: Modifier = Modifier
) {
    // Animación de entrada
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icono de cerdo con monedas
            Image(
                painter = painterResource(id = R.drawable.ic_pig),
                contentDescription = "Monedas ganadas",
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(Color(0xFFFFD700))
            )

            // Texto con las monedas ganadas
            Text(
                text = "+$coins monedas",
                color = Color(0xFFFFD700),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            // Mensaje adicional
            Text(
                text = "¡Excelente trabajo!",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}