package com.robertolopezaguilera.futbilito.ui

// TimeStoreDialog.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.robertolopezaguilera.futbilito.viewmodel.TimeStoreState

@Composable
fun TimeStoreDialog(
    timeStoreState: TimeStoreState,
    onPurchaseWithCoins: () -> Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (timeStoreState.isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = modifier.width(300.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2E3440)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    Text(
                        text = "¿Necesitas más tiempo?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información de tiempo a agregar
                    val timeToAdd = if (timeStoreState.timeExtensionsUsed == 0) 30 else 15
                    Text(
                        text = "Obtén $timeToAdd segundos extra",
                        color = Color(0xFF88C0D0),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Información de monedas del usuario
                    Text(
                        text = "Tus monedas: ${timeStoreState.userCoins}",
                        color = if (timeStoreState.userCoins >= 30) Color(0xFFA3BE8C) else Color(0xFFBF616A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de compra con monedas
                    val canPurchase = timeStoreState.userCoins >= 30
                    Button(
                        onClick = {
                            val success = onPurchaseWithCoins()
                            if (!success) {
                                // Mostrar mensaje de error si no tiene suficientes monedas
                                // Podrías agregar un snackbar aquí
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canPurchase) Color(0xFFD08770) else Color(0xFF5E6268),
                            contentColor = Color.White
                        ),
                        enabled = canPurchase
                    ) {
                        Text(
                            text = "Comprar por 30 monedas",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón de ver anuncio
                    Button(
                        onClick = onWatchAd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5E81AC),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Ver anuncio gratis",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de cancelar
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFF88C0D0)
                        )
                    }
                }
            }
        }
    }
}

// PurchaseResultDialog.kt
@Composable
fun PurchaseResultDialog(
    isVisible: Boolean,
    isSuccess: Boolean,
    timeAdded: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = modifier.width(280.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2E3440)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val iconColor = if (isSuccess) Color(0xFFA3BE8C) else Color(0xFFBF616A)
                    val title = if (isSuccess) "¡Éxito!" else "Error"
                    val message = if (isSuccess)
                        "Se agregaron $timeAdded segundos a tu tiempo"
                    else
                        "No tienes suficientes monedas"

                    Text(
                        text = title,
                        color = iconColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iconColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}