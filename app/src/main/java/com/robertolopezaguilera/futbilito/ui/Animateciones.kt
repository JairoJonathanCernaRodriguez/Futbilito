package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.R
import kotlin.math.PI
import kotlin.math.sin

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