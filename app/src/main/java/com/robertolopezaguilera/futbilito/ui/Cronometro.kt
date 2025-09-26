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
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TimeStoreViewModel
import kotlinx.coroutines.delay

@Composable
fun Cronometro(
    tiempoRestante: MutableState<Int>,
    isActive: Boolean,
    isPaused: Boolean,
    onTimeOut: () -> Unit,
    onPauseChange: (Boolean) -> Unit,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val rewardedAdState = rememberRewardedAdState()

    // 👇 Observar las monedas y usuario del ViewModel
    val usuario by gameViewModel.usuario.collectAsState()
    val monedasActuales by gameViewModel.monedas.collectAsState()

    // 👇 Nuevo ViewModel para la tienda de tiempo (SIMPLIFICADO)
    val timeStoreViewModel = remember { TimeStoreViewModel() }
    val timeStoreState by timeStoreViewModel.timeStoreState.collectAsState()

    val tiempo = tiempoRestante.value

    // Animaciones
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

    // 👇 MEJORADO: Efecto para manejar el resultado de la compra
    LaunchedEffect(timeStoreState.purchaseSuccess) {
        if (timeStoreState.purchaseSuccess) {
            val timeToAdd = if (timeStoreState.timeExtensionsUsed == 0) 30 else 15

            // Verificar si tiene suficientes monedas
            val monedasDisponibles = usuario?.monedas ?: monedasActuales
            if (monedasDisponibles >= 30) {
                // Restar monedas y agregar tiempo
                gameViewModel.restarMonedas(30)
                tiempoRestante.value += timeToAdd
                timeStoreViewModel.hideTimeStoreDialog()
                println("✅ Compra exitosa: +$timeToAdd segundos")
            } else {
                println("❌ No hay suficientes monedas para comprar")
            }

            delay(1000)
            timeStoreViewModel.resetPurchaseState()
        }
    }

    // 👇 MEJORADO: Efecto para manejar la selección de ver anuncio
    LaunchedEffect(timeStoreState.watchAdSelected) {
        if (timeStoreState.watchAdSelected && activity != null) {
            println("📺 Mostrando anuncio rewarded...")

            showRewardedAd(activity, rewardedAdState,
                onRewardEarned = { rewardItem ->
                    val timeToAdd = if (timeStoreState.timeExtensionsUsed == 0) 30 else 15
                    tiempoRestante.value += timeToAdd
                    timeStoreViewModel.timeExtensionsUsed++
                    timeStoreViewModel.hideTimeStoreDialog()
                    println("✅ Anuncio completado: +$timeToAdd segundos")
                },
                onAdDismissed = {
                    println("📺 Anuncio cerrado")
                    timeStoreViewModel.hideTimeStoreDialog()
                    onPauseChange(false)
                },
                onAdFailed = {
                    timeStoreViewModel.hideTimeStoreDialog()
                    onPauseChange(false)
                }
            )
            timeStoreViewModel.resetPurchaseState()
        }
    }

    // Control del cronómetro
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
            delay(100)
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
            // Caja del cronómetro
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
                            println("➕ Botón de tiempo clickeado")
                            // 👇 Obtener monedas actuales para mostrar en el diálogo
                            val monedasParaDialogo = usuario?.monedas ?: monedasActuales
                            println("💳 Monedas actuales: $monedasParaDialogo")

                            onPauseChange(true)
                            timeStoreViewModel.showTimeStoreDialog(monedasParaDialogo)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 👇 DIÁLOGOS
    // Diálogo de tienda de tiempo
    TimeStoreDialog(
        timeStoreState = timeStoreState,
        onPurchaseWithCoins = {
            // Verificar inmediatamente con las monedas actuales
            val monedasDisponibles = usuario?.monedas ?: monedasActuales
            val puedeComprar = monedasDisponibles >= 30

            if (puedeComprar) {
                timeStoreViewModel.purchaseTimeWithCoins(monedasDisponibles)
                true
            } else {
                println("❌ No se puede comprar: monedas insuficientes")
                false
            }
        },
        onWatchAd = {
            timeStoreViewModel.watchAdForTime()
        },
        onDismiss = {
            timeStoreViewModel.hideTimeStoreDialog()
            onPauseChange(false)
            println("❌ Diálogo de tiempo cerrado")
        }
    )

    // Diálogo de resultado de compra (opcional - puedes eliminarlo si no lo usas)
    if (timeStoreState.purchaseSuccess) {
        PurchaseResultDialog(
            isVisible = true,
            isSuccess = true,
            timeAdded = timeStoreState.timeAdded,
            onDismiss = {
                timeStoreViewModel.resetPurchaseState()
            }
        )
    }
}