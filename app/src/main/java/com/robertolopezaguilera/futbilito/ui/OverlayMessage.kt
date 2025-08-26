package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlayMessage(
    message: String,
    onRestart: () -> Unit,
    onGoToNiveles: () -> Unit,
    onGoToCategorias: () -> Unit,
    onNextLevel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                message,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Botón Reiniciar
            Button(onClick = onRestart) {
                Text("Reiniciar nivel")
            }

            // Botón Ir a lista de niveles
            Button(onClick = onGoToNiveles) {
                Text("Lista de niveles")
            }

            // Botón Ir a Categorías
            Button(onClick = onGoToCategorias) {
                Text("Categorías")
            }

            // Botón destacado para Siguiente Nivel
            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Siguiente Nivel", color = Color.White)
            }
        }
    }
}
