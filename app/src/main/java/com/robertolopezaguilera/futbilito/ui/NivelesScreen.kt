package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel
import com.robertolopezaguilera.futbilito.ui.theme.*

@Composable
private fun getColorsForCategory(categoria: String): List<Color> {
    return when (categoria) {
        "Tutorial" -> tutorialColor
        "Principiante" -> principianteColor
        "Medio" -> medioColor
        "Avanzado" -> avanzadoColor
        "Experto" -> expertoColor
        else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun NivelesScreen(
    viewModel: NivelViewModel,
    categoria: String,
    onNivelClick: (Int, Int) -> Unit
) {
    val niveles by viewModel.getNivelesPorCategoria(categoria).collectAsState(initial = emptyList())
    val colors = getColorsForCategory(categoria)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B4A),
                        Color(0xFF172B6F),
                        Color(0xFF233A89)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con mejoras (trofeo, estrellas acumuladas, progress ring)
            CategoryHeader(
                categoria = categoria,
                colors = colors,
                niveles = niveles
            )

            Spacer(modifier = Modifier.height(12.dp))

            // barra de progreso general (porcentaje de niveles completados)
            val progresoCategoria = calcularProgresoCategoria(niveles)
            LinearProgressIndicator(
                progress = { progresoCategoria },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = colors[0],
                trackColor = Color.White.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Info resumen
            Text(
                text = "${niveles.count { it.puntuacion > 0 }} de ${niveles.size} niveles completados",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Grid de niveles
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(niveles) { index, nivel ->
                    val desbloqueado = determinarSiNivelEstaDesbloqueado(niveles, index)
                    NivelCard(
                        nivel = nivel,
                        colors = colors,
                        desbloqueado = desbloqueado,
                        onNivelClick = { if (desbloqueado) onNivelClick(nivel.id, nivel.tiempo) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(categoria: String, colors: List<Color>, niveles: List<Nivel>) {
    // animación de brillo sutil
    val transition = rememberInfiniteTransition()
    val glow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.1f at 1000 with FastOutLinearInEasing
            },
            repeatMode = RepeatMode.Reverse // CORRECCIÓN: Cambiado de InfiniteRepeatMode a RepeatMode
        )
    )

    // estadisticas
    val totalStars = niveles.sumOf { (it.puntuacion.coerceIn(0, 4)).toInt() }
    val maxStars = (niveles.size.coerceAtLeast(1) * 4)
    val starsProgress = if (maxStars > 0) totalStars.toFloat() / maxStars else 0f
    val nivelesCompletados = niveles.count { it.puntuacion > 0 }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: título + subtítulo
        Column {
            Text(
                text = categoria,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$nivelesCompletados / ${niveles.size} niveles jugados",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }

        // Center: trofeo + count (estrellas totales)
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(colors[0].copy(alpha = 0.18f), colors.getOrNull(1) ?: colors[0].copy(alpha = 0.08f))))
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // trofeo/icon
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Trofeo",
                    tint = colors[0].copy(alpha = (glow * 0.9f).coerceIn(0.4f, 1f)),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "$totalStars",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Estrellas",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Right: anillo de progreso (estrellas/posible)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                progress = starsProgress,
                strokeWidth = 6.dp,
                modifier = Modifier.size(56.dp),
                color = colors[0]
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(starsProgress * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NivelCard(
    nivel: Nivel,
    colors: List<Color>,
    desbloqueado: Boolean,
    onNivelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = desbloqueado, onClick = onNivelClick)
            .shadow(
                elevation = if (desbloqueado) 12.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (desbloqueado) colors[0] else Color.Gray
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (desbloqueado) colors[0].copy(alpha = 0.20f) else Color.DarkGray.copy(alpha = 0.45f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (desbloqueado) colors else listOf(Color.Gray.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (desbloqueado) Color.White.copy(alpha = 0.18f) else Color.Gray.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (desbloqueado) {
                        Text(text = nivel.id.toString(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Bloqueado", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Nivel ${nivel.id}", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${nivel.tiempo}s", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // 5 estrellas (asumiendo puntuación 0..5)
                val estrellasObtenidas = nivel.puntuacion.coerceIn(0, 5)
                Row {
                    repeat(5) { idx ->
                        if (idx < estrellasObtenidas) {
                            // Estrella llena (recurso personalizado si lo tienes)
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "Estrella llena",
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFFD700))
                            )
                        } else {
                            // Estrella vacía (outline)
                            Image(
                                painter = painterResource(id = R.drawable.ic_linemdstar),
                                contentDescription = "Estrella vacía",
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFFD700).copy(alpha = 0.28f))
                            )
                        }
                        if (idx < 4) Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

// Determina si un nivel está desbloqueado (el anterior debe tener > 0 puntuación)
private fun determinarSiNivelEstaDesbloqueado(niveles: List<Nivel>, index: Int): Boolean {
    if (index == 0) return true
    val nivelAnterior = niveles.getOrNull(index - 1)
    return (nivelAnterior?.puntuacion ?: 0) > 0
}

// Progreso de la categoría (niveles con al menos 1 estrella)
private fun calcularProgresoCategoria(niveles: List<Nivel>): Float {
    if (niveles.isEmpty()) return 0f
    val completados = niveles.count { it.puntuacion > 0 }
    return completados.toFloat() / niveles.size
}