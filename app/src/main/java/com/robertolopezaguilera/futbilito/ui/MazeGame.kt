package com.robertolopezaguilera.futbilito.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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

    // 👇 Estados
    var isLoading by remember { mutableStateOf(true) }
    var showCoinAnimation by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableStateOf(0) }
    var pigAnimationTrigger by remember { mutableStateOf(0) }

    // 👇 MediaPlayer y Vibrator - CORREGIDO
    val coinSound = remember {
        MediaPlayer.create(context, R.raw.coin_sound).apply {
            setOnCompletionListener { it.seekTo(0) }
        }
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 🔴 Liberar recursos correctamente
    DisposableEffect(Unit) {
        onDispose {
            coinSound.stop()
            coinSound.release()
        }
    }

    // 👇 Función para efectos - CORREGIDA
    val playCoinEffects = remember {
        {
            try {
                println("🔊 EJECUTANDO EFECTOS DE SONIDO Y VIBRACIÓN")

                // Sonido
                if (coinSound.isPlaying) {
                    coinSound.seekTo(0)
                }
                coinSound.start()
                println("🔊 Sonido iniciado - isPlaying: ${coinSound.isPlaying}")

                // Vibración
                if (vibrator.hasVibrator()) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(200)
                        }
                        println("📳 Vibración ejecutada")
                    } catch (vibeException: Exception) {
                        println("❌ Error en vibración: ${vibeException.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error general en efectos: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Cargar recursos
    LaunchedEffect(nivel?.id) {
        isLoading = true
        delay(100)
        isLoading = false
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    val coinPainter = painterResource(id = R.drawable.ic_coin)
    val tiempoInicial = nivel?.tiempo ?: 60
    var puntuacionLocal by remember { mutableStateOf<Int?>(null) }
    var isGamePaused by remember { mutableStateOf(false) }

    // 🎮 GameEngine - SOLO depende del nivel ID
    val gameEngine = remember(nivel?.id) {
        println("🔄 Creando GameEngine para nivel: ${nivel?.id}")
        GameEngine(
            borderObstacles = borderObstacles.map { it.toGameObstacle() },
            obstacles = obstaclesFromDb.map { it.toGameObstacle() },
            itemsFromDb = itemsFromDb,
            spawnPoint = Offset(0f, 0f),
            onCoinCollected = {
                println("🎯 Callback recibido en MazeGame")
                playCoinEffects()
                pigAnimationTrigger++
            }
        )
    }

    // 👇 DEBUG: Verificar items recolectados
    val collectedCount = gameEngine.items.count { it.collected }
    LaunchedEffect(collectedCount) {
        println("📊 Items recolectados: $collectedCount/${gameEngine.items.size}")
    }

    // 👇 Update con sensor
    LaunchedEffect(tiltX, tiltY) {
        if (!isGamePaused && gameEngine.gameState == GameState.PLAYING) {
            gameEngine.updateWithSensor(tiltX, tiltY)
        }
    }

    // 👇 Calcular puntuación cuando se recolectan todos los items
    LaunchedEffect(collectedCount) {
        val total = gameEngine.items.size
        if (total > 0 && collectedCount == total && puntuacionLocal == null) {
            println("🎉 Todos los items recolectados!")

            val porcentajeRestante = (tiempoRestante.value.toFloat() / tiempoInicial.toFloat()) * 100f
            val score = when {
                porcentajeRestante >= 30f -> 4
                porcentajeRestante >= 20f -> 3
                porcentajeRestante >= 10f -> 2
                else -> 1
            }

            val puntuacionActualNivel = nivel?.puntuacion ?: 0
            val coins = when (score) {
                4 -> 15
                3 -> 10
                2 -> 8
                1 -> 5
                else -> 0
            }

            if (score > puntuacionActualNivel) {
                onAddCoins(coins)
                coinsEarned = coins
                showCoinAnimation = true
                delay(3000)
                showCoinAnimation = false
                puntuacionLocal = score
                onLevelScored(score)
            } else {
                puntuacionLocal = puntuacionActualNivel
            }
            gameEngine.gameState = GameState.LEVEL_COMPLETE
        }
    }

    // UI (igual que antes)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFF2E3440), size = size)
                    val originX = size.width / 2f
                    val originY = size.height / 2f

                    // Dibujar obstáculos
                    gameEngine.borderObstacles.forEach { o ->
                        drawRect(
                            color = Color(0xFF5E81AC),
                            topLeft = Offset(originX + o.x, originY + o.y),
                            size = Size(o.width, o.height)
                        )
                    }

                    gameEngine.obstacles.forEach { o ->
                        drawRect(
                            color = Color(0xFF5E81AC),
                            topLeft = Offset(originX + o.x, originY + o.y),
                            size = Size(o.width, o.height)
                        )
                    }

                    // Dibujar items no recolectados
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

                    // Dibujar pelota
                    drawCircle(
                        color = Color(0xFFBF616A),
                        radius = 16f,
                        center = Offset(originX + gameEngine.x, originY + gameEngine.y)
                    )
                }

                // Resto de tu UI
                PauseButton(
                    isPaused = isGamePaused,
                    onPauseChange = { paused ->
                        isGamePaused = paused
                        if (paused) gameEngine.pause() else gameEngine.resume()
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )

                Cronometro(
                    tiempoRestante = tiempoRestante,
                    isActive = gameEngine.gameState == GameState.PLAYING,
                    isPaused = isGamePaused,
                    onTimeOut = {
                        gameEngine.gameState = GameState.GAME_OVER
                        onTimeOut()
                    },
                    onPauseChange = { paused ->
                        isGamePaused = paused
                        if (paused) gameEngine.pause() else gameEngine.resume()
                    },
                    isOnline = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )

                // Overlay info
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedPigIcon(trigger = pigAnimationTrigger)
                        Text(
                            text = "${collectedCount} / ${gameEngine.items.size}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showCoinAnimation) {
                    CoinAnimation(
                        coins = coinsEarned,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                when (gameEngine.gameState) {
                    GameState.LEVEL_COMPLETE -> {
                        LevelCompletedOverlay(
                            starsEarned = puntuacionLocal ?: 0,
                            onRestart = onRestart,
                            onGoToNiveles = { activity?.finish() },
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
                            onGoToNiveles = { activity?.finish() },
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
            //AdBanner(modifier = Modifier.fillMaxWidth())
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