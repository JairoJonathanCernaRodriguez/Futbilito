package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.robertolopezaguilera.futbilito.R
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PauseButton(
    isPaused: Boolean,
    onPauseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación de escala para efecto de pulsación
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .size(50.dp)
            .background(
                color = if (isPaused) Color(0xFFFFA000) else Color(0xFF2196F3),
                shape = CircleShape
            )
            .clickable {
                onPauseChange(!isPaused)
            }
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (isPaused) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reanudar",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_pause),
                contentDescription = "Pausar",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}