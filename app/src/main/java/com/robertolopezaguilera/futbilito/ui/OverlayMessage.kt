package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.R
import kotlin.math.cos
import kotlin.math.sin

// Enum para los tipos de mensaje
enum class MessageType {
    LEVEL_COMPLETED, TIME_UP, GAME_OVER
}

@Composable
fun OverlayMessage(
    messageType: MessageType,
    message: String,
    starsEarned: Int = 0,
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    // Animación de entrada
    val scaleAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )

    // Obtener configuración según el tipo de mensaje (sin destructuring con 4 elementos)
    val config = when (messageType) {
        MessageType.LEVEL_COMPLETED -> MessageConfig(
            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
            iconResId = R.drawable.ic_trophy,
            title = "¡Nivel Completado!",
            particleColor = Color(0xFFFFD700)
        )
        MessageType.TIME_UP -> MessageConfig(
            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFF57C00)),
            iconResId = R.drawable.ic_timer_off,
            title = "Tiempo Agotado",
            particleColor = Color(0xFFFFB74D)
        )
        MessageType.GAME_OVER -> MessageConfig(
            gradientColors = listOf(Color(0xFFF44336), Color(0xFFD32F2F)),
            iconResId = R.drawable.ic_timer_off,
            title = "Game Over",
            particleColor = Color(0xFFFF8A65)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Partículas flotantes de fondo
        FloatingParticles(particleColor = config.particleColor)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer {
                    scaleX = scaleAnimation
                    scaleY = scaleAnimation
                }
                .background(
                    brush = Brush.verticalGradient(config.gradientColors),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(32.dp)
        ) {
            // Icono principal con animación
            AnimatedIcon(
                iconResId = config.iconResId,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(20.dp)
            )

            // Título con efecto de brillo
            GlowingText(
                text = config.title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Mensaje personalizado
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Mostrar estrellas si se completó el nivel (0-4 estrellas)
            if (messageType == MessageType.LEVEL_COMPLETED) {
                StarRating(
                    starsEarned = starsEarned.coerceIn(0, 4),
                    totalStars = 4,
                    starSize = 36.dp
                )

                // Texto de estrellas ganadas
                Text(
                    text = "¡Ganaste $starsEarned de 4 estrellas!",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción con animaciones
            ActionButtons(
                messageType = messageType,
                onRestart = onRestart,
                onGoToNiveles = onGoToNiveles,
                onGoToCategorias = onGoToCategorias,
                onNextLevel = onNextLevel
            )
        }
    }
}

// Data class para la configuración del mensaje
data class MessageConfig(
    val gradientColors: List<Color>,
    val iconResId: Int,
    val title: String,
    val particleColor: Color
)

@Composable
fun AnimatedIcon(iconResId: Int, modifier: Modifier = Modifier) {
    // Animación de flotación
    val floatAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0 with LinearEasing
                0.5f at 1000 with LinearEasing
                0f at 2000 with LinearEasing
            }
        ),
        label = "floatAnimation"
    )

    var translationY = 8f * sin(floatAnimation * 2 * Math.PI.toFloat())

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = translationY
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun GlowingText(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight) {
    val glow by rememberInfiniteTransition().animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnimation"
    )

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            alpha = glow
        }
    )
}

@Composable
fun StarRating(starsEarned: Int, totalStars: Int, starSize: androidx.compose.ui.unit.Dp) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalStars) { index ->
            AnimatedStar(
                isFilled = index < starsEarned,
                size = starSize,
                delay = index * 200
            )
            if (index < totalStars - 1) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun AnimatedStar(isFilled: Boolean, size: androidx.compose.ui.unit.Dp, delay: Int) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.2f else 0.8f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "starAnimation"
    )

    Image(
        painter = painterResource(
            id = if (isFilled) R.drawable.ic_star
            else R.drawable.ic_linemdstar
        ),
        contentDescription = "Estrella",
        colorFilter = ColorFilter.tint(
            if (isFilled) Color(0xFFFFD700)
            else Color(0xFFFFD700).copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .size(size)
            .scale(scale)
    )
}

@Composable
fun FloatingParticles(particleColor: Color = Color.White.copy(alpha = 0.3f)) {
    val particles = remember { List(15) { it } }

    particles.forEach { index ->
        val animation by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3000 + index * 200
                    0f at 0
                    1f at durationMillis
                }
            ),
            label = "particleAnimation$index"
        )

        val angle = (index * 25).toFloat()
        val radius = 200f + index * 20f
        val x = radius * cos(angle * Math.PI / 180).toFloat()
        val y = radius * sin(angle * Math.PI / 180).toFloat()

        Box(
            modifier = Modifier
                .offset(x = x.dp, y = y.dp)
                .size(8.dp)
                .graphicsLayer {
                    alpha = 1 - animation
                    scaleX = 0.5f + animation * 0.5f
                    scaleY = 0.5f + animation * 0.5f
                }
                .background(particleColor.copy(alpha = 0.6f), CircleShape)
        )
    }
}

@Composable
fun ActionButtons(
    messageType: MessageType,
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón Reiniciar (siempre visible)
        AnimatedRoundButton(
            iconResId = R.drawable.ic_replay,
            text = "Reintentar",
            backgroundColor = Color(0xFF2196F3),
            onClick = onRestart,
            delay = 100
        )

        // Botones de navegación
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedRoundButton(
                iconVector = Icons.Filled.ArrowBack,
                text = "Niveles",
                backgroundColor = Color(0xFF9C27B0),
                onClick = onGoToNiveles,
                delay = 200,
                isSmall = true
            )

            AnimatedRoundButton(
                iconVector = Icons.Filled.Home,
                text = "Categorías",
                backgroundColor = Color(0xFF607D8B),
                onClick = onGoToCategorias,
                delay = 300,
                isSmall = true
            )
        }

        // Botón Siguiente Nivel (solo para nivel completado)
        if (messageType == MessageType.LEVEL_COMPLETED) {
            AnimatedRoundButton(
                iconVector = Icons.Filled.ArrowForward,
                text = "Siguiente Nivel",
                backgroundColor = Color(0xFF4CAF50),
                onClick = onNextLevel,
                delay = 400
            )
        }
    }
}

@Composable
fun AnimatedRoundButton(
    iconVector: ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    delay: Int,
    isSmall: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "buttonAnimation"
    )

    RoundButton(
        iconVector = iconVector,
        text = text,
        backgroundColor = backgroundColor,
        onClick = onClick,
        isSmall = isSmall,
        modifier = Modifier.scale(scale)
    )
}

@Composable
fun AnimatedRoundButton(
    iconResId: Int,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    delay: Int,
    isSmall: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "buttonAnimation"
    )

    RoundButton(
        iconResId = iconResId,
        text = text,
        backgroundColor = backgroundColor,
        onClick = onClick,
        isSmall = isSmall,
        modifier = Modifier.scale(scale)
    )
}

// FUNCIÓN ROUNDBUTTON QUE FALTABA
@Composable
fun RoundButton(
    iconVector: ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonSize = if (isSmall) 60.dp else 70.dp
    val iconSize = if (isSmall) 24.dp else 28.dp
    val textSize = if (isSmall) 12.sp else 14.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .shadow(8.dp, CircleShape)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = textSize,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RoundButton(
    iconResId: Int,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonSize = if (isSmall) 60.dp else 70.dp
    val iconSize = if (isSmall) 24.dp else 28.dp
    val textSize = if (isSmall) 12.sp else 14.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .shadow(8.dp, CircleShape)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(iconSize)
            )
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = textSize,
            fontWeight = FontWeight.Medium
        )
    }
}

// Función de conveniencia para mensaje de nivel completado (0-4 estrellas)
@Composable
fun LevelCompletedOverlay(
    message: String,
    starsEarned: Int, // Ahora de 0 a 4 estrellas
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    OverlayMessage(
        messageType = MessageType.LEVEL_COMPLETED,
        message = message,
        starsEarned = starsEarned.coerceIn(0, 4),
        onRestart = onRestart,
        onGoToNiveles = onGoToNiveles,
        onGoToCategorias = onGoToCategorias,
        onNextLevel = onNextLevel
    )
}

// Función de conveniencia para mensaje de tiempo agotado
@Composable
fun TimeUpOverlay(
    message: String,
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    OverlayMessage(
        messageType = MessageType.TIME_UP,
        message = message,
        onRestart = onRestart,
        onGoToNiveles = onGoToNiveles,
        onGoToCategorias = onGoToCategorias,
        onNextLevel = onNextLevel
    )
}

// Función de conveniencia para mensaje de game over
@Composable
fun GameOverOverlay(
    message: String,
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    OverlayMessage(
        messageType = MessageType.GAME_OVER,
        message = message,
        onRestart = onRestart,
        onGoToNiveles = onGoToNiveles,
        onGoToCategorias = onGoToCategorias,
        onNextLevel = onNextLevel
    )
}