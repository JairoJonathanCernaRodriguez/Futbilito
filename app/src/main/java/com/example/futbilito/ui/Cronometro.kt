package com.example.futbilito.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun Cronometro(
    tiempoRestante: MutableState<Int>,
    isActive: Boolean,
    onTimeOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateFloatAsState(
        targetValue = when {
            tiempoRestante.value <= 10 -> 1f
            tiempoRestante.value <= 30 -> 0.7f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textColorAnimation"
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            while (tiempoRestante.value > 0) {
                delay(1000)
                if (isActive) { // Verificar nuevamente por si cambió el estado
                    tiempoRestante.value--
                }
            }
            if (isActive) {
                onTimeOut()
            }
        }
    }

    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("TIEMPO: ")
                }
                withStyle(
                    style = SpanStyle(
                        color = when {
                            tiempoRestante.value <= 10 -> Color.Red
                            tiempoRestante.value <= 30 -> Color.Red.copy(alpha = textColor)
                            else -> Color.White
                        },
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(tiempoRestante.value.toString())
                }
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("s")
                }
            },
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}