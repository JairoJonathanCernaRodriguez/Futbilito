package com.robertolopezaguilera.futbilito.ui

import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.admob.showRewardedAd
import com.robertolopezaguilera.futbilito.admob.rememberRewardedAdState
import kotlinx.coroutines.delay

@Composable
fun Cronometro(
    tiempoRestante: MutableState<Int>,
    isActive: Boolean,
    isPaused: Boolean,
    onTimeOut: () -> Unit,
    onPauseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val rewardedAdState = rememberRewardedAdState()
    val tiempo = tiempoRestante.value

    // Animaciones (igual que antes)
    val blinkAlpha by animateFloatAsState(
        targetValue = if (tiempo in 1..10) 1f else 0f,
        animationSpec = if (tiempo in 1..10)
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0f at 0
                    1f at 500
                    0f at 1000
                },
                repeatMode = RepeatMode.Reverse
            )
        else tween(300),
        label = "blinkAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (tiempo in 1..15) 1.12f else 1f,
        animationSpec = if (tiempo in 1..15)
            infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse)
        else tween(250),
        label = "scaleAnim"
    )

    // Control del cronómetro - VERSIÓN CORREGIDA
    LaunchedEffect(isActive, isPaused) {
        var lastUpdateTime = System.currentTimeMillis()

        while (isActive && tiempoRestante.value > 0) {
            if (!isPaused) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastUpdateTime

                if (elapsed >= 1000) {
                    tiempoRestante.value = tiempoRestante.value - 1
                    lastUpdateTime = currentTime

                    if (tiempoRestante.value == 0) {
                        onTimeOut()
                        break
                    }
                }
            }
            delay(100) // Pequeño delay para no consumir demasiados recursos
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            // Caja del cronómetro (igual que antes)
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            tiempo in 1..10 -> Color.Red.copy(alpha = blinkAlpha)
                            tiempo <= 30 -> Color.Yellow
                            else -> Color.White
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .scale(scaleAnim)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = if (tiempo <= 10) Color.Red else Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append(tiempo.toString()) }
                        withStyle(
                            style = SpanStyle(color = Color.White, fontSize = 18.sp)
                        ) { append("s") }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (isOnline && activity != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                        .size(36.dp)
                        .background(Color(0xFF2ECC71), shape = CircleShape)
                        .clickable {
                            Log.d("RewardedAd", "Botón clickeado")
                            // PAUSAR el juego
                            onPauseChange(true)

                            showRewardedAd(activity, rewardedAdState,
                                onRewardEarned = { rewardItem ->
                                    Log.d("RewardedAd", "Añadiendo 30 segundos")
                                    tiempoRestante.value += 30
                                },
                                onAdDismissed = {
                                    // REANUDAR si se cierra el anuncio
                                    onPauseChange(false)
                                },
                                onAdFailed = {
                                    // REANUDAR si falla
                                    onPauseChange(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}