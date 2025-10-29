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
import com.robertolopezaguilera.futbilito.PowerType
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.admob.AdBanner
import com.robertolopezaguilera.futbilito.data.GamePersonalizacion
import com.robertolopezaguilera.futbilito.data.Item
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.data.Powers
import com.robertolopezaguilera.futbilito.toGameObstacle
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModel
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
    powersFromDb: List<Powers>,
    tiempoRestante: MutableState<Int>,
    onTimeOut: () -> Unit,
    onRestart: () -> Unit,
    tiltX: Float,
    tiltY: Float,
    onLevelScored: (Int) -> Unit = {},
    onAddCoins: (Int) -> Unit = {},
    gameViewModel: GameViewModel,
    tiendaViewModel: TiendaViewModel? = null // 👈 NUEVO: Recibir el ViewModel de tienda
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 👇 NUEVO: Cargar personalizaciones desde la tienda
    val personalizacion = remember(tiendaViewModel) {
        if (tiendaViewModel != null) {
            GamePersonalizacion(
                colorFondo = tiendaViewModel.getColorFondoSeleccionado(),
                colorPelota = tiendaViewModel.getColorPelotaSeleccionado(),
                colorObstaculos = tiendaViewModel.getColorObstaculoSeleccionado(),
                iconoPowerUp = R.drawable.ic_power,
                iconoFantasma = R.drawable.ic_ghost,
                iconoPelota = tiendaViewModel.getIconoPelotaSeleccionado() // 👈 NUEVO
            )
        } else {
            // Valores por defecto si no hay tiendaViewModel
            GamePersonalizacion()
        }
    }

    // 👇 Estados
    var isLoading by remember { mutableStateOf(true) }
    var showCoinAnimation by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableStateOf(0) }
    var pigAnimationTrigger by remember { mutableStateOf(0) }
    var activePowerType by remember { mutableStateOf<PowerType?>(null) }

    // 👇 MediaPlayer y Vibrator
    val coinSound = remember {
        MediaPlayer.create(context, R.raw.coin_sound).apply {
            setOnCompletionListener { it.seekTo(0) }
        }
    }

    val powerSound = remember {
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
            powerSound.stop()
            powerSound.release()
        }
    }

    // 👇 CORREGIDO: Separar la lógica del timer del efecto de sonido
    var powerTrigger by remember { mutableStateOf(0) }
    var lastPowerType by remember { mutableStateOf<PowerType?>(null) }

    // 👇 Timer para limpiar el UI del poder activo (fuera de la lambda)
    LaunchedEffect(powerTrigger) {
        lastPowerType?.let { powerType ->
            delay(when (powerType) {
                PowerType.SPEED_BOOST -> 10000L
                PowerType.GHOST_MODE -> 8000L
                else -> 0L
            })
            activePowerType = null
            lastPowerType = null
        }
    }

    // 👇 Función para efectos de monedas
    val playCoinEffects = {
        try {
            println("🔊 Efectos de moneda")
            if (coinSound.isPlaying) coinSound.seekTo(0)
            coinSound.start()

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        } catch (e: Exception) {
            println("❌ Error en efectos: ${e.message}")
        }
    }

    // 👇 CORREGIDO: Función simple para efectos de poder
    val playPowerEffects = { powerType: PowerType ->
        try {
            println("⚡ Efectos de power-up: $powerType")
            if (powerSound.isPlaying) powerSound.seekTo(0)
            powerSound.start()

            // Vibración más larga para poderes
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }

            // Actualizar UI con el poder activo
            activePowerType = powerType
            lastPowerType = powerType
            powerTrigger++ // 👈 Disparar el timer

        } catch (e: Exception) {
            println("❌ Error en efectos de poder: ${e.message}")
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

    // 👇 NUEVO: Usar iconos personalizados desde la tienda
    val coinPainter = painterResource(id = R.drawable.ic_coin)
    val ghostPainter = painterResource(id = personalizacion.iconoFantasma)
    val velocidadPainter = painterResource(id = personalizacion.iconoPowerUp)

    val tiempoInicial = nivel?.tiempo ?: 60
    var puntuacionLocal by remember { mutableStateOf<Int?>(null) }
    var isGamePaused by remember { mutableStateOf(false) }

    // 🎮 GameEngine
    val gameEngine = remember(nivel?.id) {
        println("🔄 Creando GameEngine para nivel: ${nivel?.id}")
        GameEngine(
            borderObstacles = borderObstacles.map { it.toGameObstacle() },
            obstacles = obstaclesFromDb.map { it.toGameObstacle() },
            itemsFromDb = itemsFromDb,
            powersFromDb = powersFromDb,
            spawnPoint = Offset(0f, 0f),
            onCoinCollected = {
                println("🎯 Callback recibido en MazeGame")
                playCoinEffects()
                pigAnimationTrigger++
            },
            onPowerCollected = { powerType ->
                println("⚡ Power-up activado: $powerType")
                playPowerEffects(powerType)
            }
        )
    }

    // 👇 DEBUG: Verificar items y poderes recolectados
    val collectedCount = gameEngine.items.count { it.collected }
    val collectedPowers = gameEngine.powers.count { it.collected }

    LaunchedEffect(collectedCount, collectedPowers) {
        println("📊 Items: $collectedCount/${gameEngine.items.size}, Poderes: $collectedPowers/${gameEngine.powers.size}")
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

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val ballIconRes = tiendaViewModel?.getIconoSeleccionado() ?: R.drawable.ic_star
            val ballIconPainter = painterResource(id = ballIconRes)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 👇 NUEVO: Usar color de fondo personalizado
                    drawRect(color = personalizacion.colorFondo, size = size)
                    val originX = size.width / 2f
                    val originY = size.height / 2f

                    // 👇 NUEVO: Dibujar obstáculos con color personalizado
                    gameEngine.borderObstacles.forEach { o ->
                        drawRect(
                            color = personalizacion.colorObstaculos,
                            topLeft = Offset(originX + o.x, originY + o.y),
                            size = Size(o.width, o.height)
                        )
                    }

                    gameEngine.obstacles.forEach { o ->
                        drawRect(
                            color = personalizacion.colorObstaculos,
                            topLeft = Offset(originX + o.x, originY + o.y),
                            size = Size(o.width, o.height)
                        )
                    }

                    // 👇 DIBUJAR PODERES NO RECOLECTADOS (con iconos personalizados)
                    gameEngine.powers.forEach { power ->
                        if (!power.collected) {
                            val powerSize = power.radius * 2
                            translate(
                                left = originX + power.x - power.radius,
                                top = originY + power.y - power.radius
                            ) {
                                // Seleccionar icono según el tipo de poder
                                val painter = when (power.type) {
                                    PowerType.SPEED_BOOST -> velocidadPainter
                                    PowerType.GHOST_MODE -> ghostPainter
                                    else -> coinPainter // Por defecto
                                }
                                with(painter) {
                                    draw(size = Size(powerSize, powerSize))
                                }
                            }
                        }
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

                    // 👇 CORREGIDO: Dibujar pelota con icono personalizado en lugar de círculo
                    val ballSize = 40f // Tamaño del icono de la pelota

                    // 👇 Aplicar efectos de color según el poder activo
                    val iconTint = when (gameEngine.activePower) {
                        PowerType.SPEED_BOOST -> Color(0xFFFFA500) // Naranja para velocidad
                        PowerType.GHOST_MODE -> Color(0xFF800080)  // Púrpura para fantasma
                        else -> Color.White // Color normal (sin tint)
                    }

                    translate(
                        left = originX + gameEngine.x - ballSize / 2,
                        top = originY + gameEngine.y - ballSize / 2
                    ) {
                        with(ballIconPainter) {
                            if (iconTint == Color.White) {
                                // Dibujar sin tint (color original del icono)
                                draw(size = Size(ballSize, ballSize))
                            } else {
                                // Dibujar con tint para efectos de poder
                                draw(
                                    size = Size(ballSize, ballSize),
                                    colorFilter = ColorFilter.tint(iconTint)
                                )
                            }
                        }
                    }

                    // 👇 EFECTO ESPECIAL para modo fantasma (aura) - mantener alrededor del icono
                    if (gameEngine.activePower == PowerType.GHOST_MODE) {
                        drawCircle(
                            color = Color(0x40800080), // Púrpura semitransparente
                            radius = ballSize / 2 + 8f, // Aura un poco más grande que el icono
                            center = Offset(originX + gameEngine.x, originY + gameEngine.y),
                            alpha = 0.3f
                        )
                    }

                    // 👇 EFECTO ESPECIAL para velocidad (aura naranja)
                    if (gameEngine.activePower == PowerType.SPEED_BOOST) {
                        drawCircle(
                            color = Color(0x40FFA500), // Naranja semitransparente
                            radius = ballSize / 2 + 6f,
                            center = Offset(originX + gameEngine.x, originY + gameEngine.y),
                            alpha = 0.4f
                        )
                    }
                }

                // 👇 INDICADOR DE PODER ACTIVO (con iconos personalizados)
                activePowerType?.let { power ->
                    val powerInfo = gameEngine.getActivePowerInfo()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Icono del poder activo (personalizado)
                            Image(
                                painter = when (power) {
                                    PowerType.SPEED_BOOST -> velocidadPainter
                                    PowerType.GHOST_MODE -> ghostPainter
                                    else -> coinPainter
                                },
                                contentDescription = "Power activo",
                                modifier = Modifier.size(24.dp)
                            )

                            // Texto descriptivo
                            Text(
                                text = when (power) {
                                    PowerType.SPEED_BOOST -> "Velocidad aumentada"
                                    PowerType.GHOST_MODE -> "Modo fantasma"
                                    else -> "Power activo"
                                },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Tiempo restante
                            Text(
                                text = "${powerInfo.remainingTime.toInt()}s",
                                color = Color.Yellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 👇 CONTADOR DE PODERES RECOLECTADOS (con icono personalizado)
                if (collectedPowers > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 80.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = velocidadPainter, // 👈 Icono personalizado
                                contentDescription = "Poderes recolectados",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = " $collectedPowers",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Resto de tu UI (sin cambios)
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
                    gameViewModel = gameViewModel,
                    isOnline = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )

                // Overlay info (items recolectados)
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