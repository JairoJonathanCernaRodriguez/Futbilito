package com.robertolopezaguilera.futbilito.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel
import com.robertolopezaguilera.futbilito.ui.theme.*
import com.robertolopezaguilera.futbilito.R

// Función para obtener colores según la categoría
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

    // Obtener colores según la categoría
    val colors = getColorsForCategory(categoria)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF303F9F)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con información de la categoría
            Text(
                text = categoria,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso de la categoría
            val progresoCategoria = calcularProgresoCategoria(niveles)
            Text(
                text = "Progreso: ${(progresoCategoria * 100).toInt()}%",
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LinearProgressIndicator(
                progress = { progresoCategoria },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors[0],
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de niveles
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
fun NivelCard(
    nivel: Nivel,
    colors: List<Color>,
    desbloqueado: Boolean,
    onNivelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = desbloqueado, onClick = onNivelClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (desbloqueado) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(colors),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Número del nivel
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (desbloqueado) Color.White.copy(alpha = 0.2f)
                            else Color.Gray.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (desbloqueado) {
                        Text(
                            text = nivel.id.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Información del nivel
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Nivel ${nivel.id}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tiempo: ${nivel.tiempo}s",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sistema de 5 estrellas con iconos personalizados
                    if (nivel.puntuacion > 0) {
                        val estrellasCompletas = nivel.puntuacion / 20 // 100/5 = 20 puntos por estrella

                        Row {
                            repeat(5) { index ->
                                if (index < estrellasCompletas) {
                                    // Estrella llena
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_star),
                                        contentDescription = "Estrella llena",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(Color(0xFFFFD700))
                                    )
                                } else {
                                    // Estrella contorno
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_linemdstar),
                                        contentDescription = "Estrella vacía",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.5f))
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    } else if (desbloqueado) {
                        Text(
                            text = "¡Nuevo! Completa para desbloquear el siguiente",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Completa el nivel anterior con al menos 1 estrella",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Indicador de estado/acción
                if (desbloqueado) {
                    if (nivel.puntuacion > 0) {
                        // Mostrar puntuación si ya se jugó
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${nivel.puntuacion}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Icono de play para niveles desbloqueados pero no jugados
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Jugar",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

// Función para determinar si un nivel está desbloqueado
private fun determinarSiNivelEstaDesbloqueado(niveles: List<Nivel>, index: Int): Boolean {
    // El primer nivel siempre está desbloqueado
    if (index == 0) return true

    // Verificar si el nivel anterior existe y tiene al menos 1 estrella (20 puntos)
    val nivelAnterior = niveles.getOrNull(index - 1)
    return nivelAnterior?.puntuacion ?: 0 >= 20
}

// Función para calcular el progreso general de la categoría
private fun calcularProgresoCategoria(niveles: List<Nivel>): Float {
    if (niveles.isEmpty()) return 0f

    // Contar niveles con al menos 1 estrella (20 puntos)
    val nivelesCompletados = niveles.count { it.puntuacion >= 20 }
    return nivelesCompletados.toFloat() / niveles.size
}