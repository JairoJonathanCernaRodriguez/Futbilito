package com.robertolopezaguilera.futbilito.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel

// Definir colores para cada categoría
val tutorialColor = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
val principianteColor = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
val medioColor = listOf(Color(0xFFFF9800), Color(0xFFFFC107))
val avanzadoColor = listOf(Color(0xFFF44336), Color(0xFFE91E63))
val expertoColor = listOf(Color(0xFF9C27B0), Color(0xFF673AB7))

@Composable
fun CategoriasScreen(
    viewModel: NivelViewModel,
    onCategoriaClick: (String) -> Unit
) {
    val categorias by viewModel.categoriasConProgreso.collectAsState()

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
            Text(
                text = "Selecciona una Dificultad",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categorias) { categoria ->
                    CategoriaCard(
                        categoria = categoria,
                        onCategoriaClick = onCategoriaClick
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriaCard(
    categoria: CategoriaConProgreso,
    onCategoriaClick: (String) -> Unit
) {
    // Determinar colores según la dificultad
    val colors = when (categoria.dificultad) {
        "Tutorial" -> tutorialColor
        "Principiante" -> principianteColor
        "Medio" -> medioColor
        "Avanzado" -> avanzadoColor
        "Experto" -> expertoColor
        else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    }

    // Verificar si la categoría está bloqueada (progreso 0% y no es Tutorial)
    val isLocked = categoria.progreso == 0f && categoria.dificultad != "Tutorial"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLocked,
                onClick = { onCategoriaClick(categoria.dificultad) }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoria.dificultad,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (categoria.progreso >= 1f) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completado",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Jugar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Niveles: ${categoria.totalNiveles}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Text(
                        text = "${categoria.puntosObtenidos}/${categoria.puntosTotales} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barra de progreso con color dinámico
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    LinearProgressIndicator(
                        progress = { categoria.progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = if (categoria.progreso >= 1f) Color(0xFF00E676) else Color.White,
                        trackColor = Color.Transparent
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isLocked) "Completa la anterior" else "${(categoria.progreso * 100).toInt()}% completado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )

                    Text(
                        text = "${(categoria.progreso * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class CategoriaConProgreso(
    val dificultad: String,
    val totalNiveles: Int,
    val puntosObtenidos: Int,
    val puntosTotales: Int
) {
    val progreso: Float
        get() = if (puntosTotales > 0) puntosObtenidos.toFloat() / puntosTotales else 0f
}