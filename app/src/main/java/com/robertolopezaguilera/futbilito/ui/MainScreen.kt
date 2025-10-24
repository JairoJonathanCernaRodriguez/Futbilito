package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.SoundManager
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel

@Composable
fun MainScreen(
    usuario: Usuario?,
    gameViewModel: GameViewModel,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit
) {
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D1B4A),
                Color(0xFF172B6F),
                Color(0xFF233A89)
            )
        )
    }

    // Animación de partículas flotantes
    val infiniteTransition = rememberInfiniteTransition()
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Partículas decorativas de fondo
        FloatingParticles(particleColor = Color.White.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header con información del usuario
            UserHeader(usuario, gameViewModel)

            Spacer(modifier = Modifier.weight(0.3f))

            // Logo y título del juego
            GameTitle()

            Spacer(modifier = Modifier.weight(0.4f))

            // Botones principales
            MainActions(
                onPlayClick = onPlayClick,
                onSettingsClick = onSettingsClick,
                onShopClick = onShopClick
            )

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

// ... (FloatingParticles, ParticleInfo, AnimatedParticle se mantienen igual)

@Composable
private fun UserHeader(usuario: Usuario?, gameViewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Información del usuario
        Column {
            Text(
                text = "¡Hola, ${usuario?.nombre ?: "Jugador"}!",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Monedas del usuario
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_coin),
                    contentDescription = "Monedas",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${usuario?.monedas ?: 0}",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GameTitle() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo del juego
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .shadow(16.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Título del juego
        Text(
            text = "FUTBILITO",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .shadow(4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Laberinto de Pelota",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MainActions(
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón PLAY principal
        AnimatedPlayButton(
            onClick = {
                soundManager.playSelectSound()
                onPlayClick()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botones secundarios
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImprovedSecondaryButton(
                icon = Icons.Filled.ShoppingCart,
                text = "Tienda",
                onClick = {
                    soundManager.playSelectSound()
                    onShopClick()
                }
            )

            ImprovedSecondaryButton(
                icon = Icons.Filled.Settings,
                text = "Ajustes",
                onClick = {
                    soundManager.playSelectSound()
                    onSettingsClick()
                }
            )
        }
    }
}

@Composable
private fun AnimatedPlayButton(onClick: () -> Unit) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            scale.animateTo(0.9f, animationSpec = tween(100))
            scale.animateTo(1.1f, animationSpec = tween(200))
            scale.animateTo(1f, animationSpec = tween(100))
            isAnimating = false
        }
    }

    // 🔹 SOLUCIÓN: Usar Box con clickable sin ripple
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(70.dp)
            .scale(scale.value)
            .shadow(16.dp, RoundedCornerShape(35.dp))
            .clip(RoundedCornerShape(35.dp))
            .background(Color(0xFF00E676))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 🔹 Elimina el efecto ripple/oscuro
            ) {
                isAnimating = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Jugar",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "JUGAR",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 🔹 NUEVO: Botón secundario mejorado
@Composable
private fun ImprovedSecondaryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            scale.animateTo(0.9f, animationSpec = tween(50))
            scale.animateTo(1f, animationSpec = tween(150))
            isAnimating = false
        }
    }

    // 🔹 SOLUCIÓN: Usar Box en lugar de Card para mejor control
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale.value)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .background(
                color = Color(0x40FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 🔹 Elimina el efecto ripple/oscuro
            ) {
                isAnimating = true
                onClick()
            },
        contentAlignment = Alignment.Center // 🔹 Centrado perfecto
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 🔹 ALTERNATIVA: Versión con efecto de elevación en lugar de sombra
@Composable
private fun ElevatedSecondaryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            scale.animateTo(0.95f, animationSpec = tween(50))
            scale.animateTo(1f, animationSpec = tween(150))
            isAnimating = false
        }
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale.value)
            .background(
                color = if (isPressed) Color(0x60FFFFFF) else Color(0x40FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .graphicsLayer {
                shadowElevation = if (isPressed) 4.dp.toPx() else 8.dp.toPx()
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isAnimating = true
                onClick()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}