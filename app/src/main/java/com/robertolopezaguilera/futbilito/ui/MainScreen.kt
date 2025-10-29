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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // 🔹 OPTIMIZACIÓN: Partículas más simples para mejor rendimiento
        SimpleFloatingParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            UserHeader(usuario)
            Spacer(modifier = Modifier.weight(0.3f))
            GameTitle()
            Spacer(modifier = Modifier.weight(0.4f))
            MainActions(
                onPlayClick = onPlayClick,
                onSettingsClick = onSettingsClick,
                onShopClick = onShopClick
            )
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

// 🔹 VERSIÓN OPTIMIZADA: Partículas más simples
@Composable
private fun SimpleFloatingParticles() {
    val particles = remember { List(5) { it } } // 🔹 REDUCIDO: Menos partículas

    particles.forEach { index ->
        val infiniteTransition = rememberInfiniteTransition()
        val offset by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000 + index * 1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = (100 + index * 70).dp,
                    y = (150 + index * 50 + offset).dp
                )
                .size((8 + index * 2).dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun UserHeader(usuario: Usuario?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "¡Hola, ${usuario?.nombre ?: "Jugador"}!",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Monedas del usuario
        Box(
            modifier = Modifier
                .background(
                    color = Color(0x40FFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
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
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .shadow(16.dp, CircleShape, clip = true) // 🔹 CORRECCIÓN: clip = true
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

        Text(
            text = "FUTBILITO",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
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
        OptimizedPlayButton(
            onClick = {
                soundManager.playSelectSound()
                onPlayClick()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FixedSecondaryButton(
                icon = Icons.Filled.ShoppingCart,
                text = "Tienda",
                onClick = {
                    soundManager.playSelectSound()
                    onShopClick()
                }
            )

            FixedSecondaryButton(
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

// 🔹 BOTÓN PLAY OPTIMIZADO
@Composable
private fun OptimizedPlayButton(onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope() // 🔹 NUEVO: Coroutine scope

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(70.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(35.dp),
                clip = true
            )
            .background(
                color = Color(0xFF00E676),
                shape = RoundedCornerShape(35.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // 🔹 CORRECCIÓN: Usar coroutineScope para las animaciones
                coroutineScope.launch {
                    scale.animateTo(0.95f, tween(100))
                    scale.animateTo(1f, tween(100))
                }
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

@Composable
private fun FixedSecondaryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope() // 🔹 NUEVO: Coroutine scope

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            )
            .background(
                color = Color(0x40FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // 🔹 CORRECCIÓN: Usar coroutineScope para las animaciones
                coroutineScope.launch {
                    scale.animateTo(0.95f, tween(50))
                    scale.animateTo(1f, tween(100))
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
                fontWeight = FontWeight.Medium
            )
        }
    }
}