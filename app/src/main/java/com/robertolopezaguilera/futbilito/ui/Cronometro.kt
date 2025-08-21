package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

@Composable
fun Cronometro(
    tiempoRestante: MutableState<Int>,
    isActive: Boolean,
    onTimeOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación para el parpadeo en los últimos 10 segundos
    val blinkAnimation by animateFloatAsState(
        targetValue = if (tiempoRestante.value <= 10 && tiempoRestante.value > 0) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0
                1f at 500
                0f at 1000
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAnimation"
    )

    // Animación de escala cuando cambia el tiempo
    val scaleAnimation by animateFloatAsState(
        targetValue = if (isActive && tiempoRestante.value > 0) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )

    // Control del cronómetro
    LaunchedEffect(isActive, tiempoRestante.value) {
        if (isActive && tiempoRestante.value > 0) {
            delay(1000)
            tiempoRestante.value--
            if (tiempoRestante.value == 0) {
                onTimeOut()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    color = when {
                        tiempoRestante.value <= 10 -> Color.Red.copy(alpha = blinkAnimation)
                        tiempoRestante.value <= 30 -> Color.Yellow
                        else -> Color.White
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = when {
                                tiempoRestante.value <= 10 -> Color.Red.copy(alpha = blinkAnimation)
                                tiempoRestante.value == 30 -> Color.Red
                                tiempoRestante.value < 30 -> Color.White
                                else -> Color.White
                            },
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(tiempoRestante.value.toString())
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("s")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}