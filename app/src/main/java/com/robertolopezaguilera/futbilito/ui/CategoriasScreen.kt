package com.robertolopezaguilera.futbilito.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.SoundManager
import com.robertolopezaguilera.futbilito.ui.theme.*
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel
import kotlinx.coroutines.delay

@Composable
fun CategoriasScreen(
    viewModel: NivelViewModel,
    onCategoriaClick: (String) -> Unit
) {
    val categorias by viewModel.categoriasConProgreso.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    // 🔹 KEY OPTIMIZATION: Remember the background brush to avoid recomputation
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D1B4A),
                Color(0xFF172B6F),
                Color(0xFF233A89)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 🔹 OPTIMIZATION: Extract header to avoid recomputation
            CategoriesHeader()

            // 🔹 KEY OPTIMIZATION: Use keys for LazyColumn items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = categorias,
                    key = { it.dificultad } // 🔹 Important for performance
                ) { categoria ->
                    OptimizedCategoriaCard(
                        categoria = categoria,
                        soundManager = soundManager,
                        onCategoriaClick = onCategoriaClick
                    )
                }
            }
        }
    }
}

// 🔹 OPTIMIZATION: Extract header to separate composable
@Composable
private fun CategoriesHeader() {
    Text(
        text = "Selecciona una Dificultad",
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .padding(vertical = 24.dp)
            .fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

// 🔹 OPTIMIZATION: Combined animated card with better state management
@Composable
private fun OptimizedCategoriaCard(
    categoria: CategoriaConProgreso,
    soundManager: SoundManager,
    onCategoriaClick: (String) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "cardAnimation_${categoria.dificultad}"
    )

    // 🔹 SOLUCIÓN: Obtener MaterialTheme fuera del remember
    val colorScheme = MaterialTheme.colorScheme

    // 🔹 OPTIMIZATION: Remember derived values
    val colors = remember(categoria.dificultad, colorScheme) {
        when (categoria.dificultad) {
            "Tutorial" -> tutorialColor
            "Principiante" -> principianteColor
            "Medio" -> medioColor
            "Avanzado" -> avanzadoColor
            "Experto" -> expertoColor
            else -> listOf(colorScheme.primary, colorScheme.secondary)
        }
    }

    val isLocked = !categoria.isUnlocked

    Box(
        modifier = Modifier
            .scale(scale)
            .padding(horizontal = 8.dp)
    ) {
        OptimizedCategoriaCardContent(
            categoria = categoria,
            colors = colors,
            isLocked = isLocked,
            onCategoriaClick = {
                soundManager.playSelectSound()
                onCategoriaClick(categoria.dificultad)
            }
        )
    }
}

// 🔹 OPTIMIZATION: Separate content to control recomposition
@Composable
private fun OptimizedCategoriaCardContent(
    categoria: CategoriaConProgreso,
    colors: List<Color>,
    isLocked: Boolean,
    onCategoriaClick: () -> Unit
) {
    // 🔹 OPTIMIZATION: Remember gradient brushes
    val backgroundBrush = remember(colors, isLocked) {
        if (isLocked) {
            Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.7f), Color.DarkGray.copy(alpha = 0.7f)))
        } else {
            Brush.verticalGradient(colors)
        }
    }

    val iconBackgroundColor = remember(colors, isLocked) {
        if (isLocked) Color.Gray.copy(alpha = 0.5f) else colors[0].copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLocked,
                onClick = onCategoriaClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush, shape = RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 🔹 OPTIMIZATION: Pass pre-computed values
                OptimizedCategoryHeader(
                    categoria = categoria,
                    isLocked = isLocked,
                    iconBackgroundColor = iconBackgroundColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                OptimizedProgressInfo(categoria, isLocked)

                Spacer(modifier = Modifier.height(16.dp))

                OptimizedProgressBar(categoria.progreso, isLocked)

                Spacer(modifier = Modifier.height(8.dp))

                OptimizedProgressPercentage(categoria, isLocked)
            }
        }
    }
}

// 🔹 OPTIMIZATION: Optimized header with pre-computed values
@Composable
private fun OptimizedCategoryHeader(
    categoria: CategoriaConProgreso,
    isLocked: Boolean,
    iconBackgroundColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = if (isLocked) Icons.Default.Lock else Icons.Filled.Star
                val tintColor = if (isLocked) Color.White.copy(alpha = 0.7f) else Color.White

                Icon(
                    imageVector = icon,
                    contentDescription = if (isLocked) "Bloqueado" else "Dificultad",
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = categoria.dificultad,
                style = MaterialTheme.typography.titleLarge,
                color = if (isLocked) Color.White.copy(alpha = 0.7f) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 🔹 OPTIMIZATION: Remember status icon
        val statusIcon = remember(categoria.progreso, isLocked) {
            when {
                isLocked -> Icons.Default.Lock to Color.White.copy(alpha = 0.8f)
                categoria.progreso >= 1f -> Icons.Default.CheckCircle to Color(0xFF00E676)
                else -> Icons.Default.ArrowForward to Color.White
            }
        }

        Icon(
            imageVector = statusIcon.first,
            contentDescription = when {
                isLocked -> "Bloqueado"
                categoria.progreso >= 1f -> "Completado"
                else -> "Jugar"
            },
            tint = statusIcon.second,
            modifier = Modifier.size(24.dp)
        )
    }
}

// 🔹 OPTIMIZATION: Optimized progress info
@Composable
private fun OptimizedProgressInfo(
    categoria: CategoriaConProgreso,
    isLocked: Boolean
) {
    // 🔹 OPTIMIZATION: Pre-calculate values
    val nivelesCompletados = remember(categoria) {
        (categoria.totalNiveles * categoria.progreso).toInt()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OptimizedInfoItem(
            value = categoria.totalNiveles.toString(),
            label = "Niveles",
            isLocked = isLocked
        )

        OptimizedInfoItem(
            value = "${categoria.puntosObtenidos}/${categoria.puntosTotales}",
            label = "Estrellas",
            isLocked = isLocked
        )

        OptimizedInfoItem(
            value = "$nivelesCompletados/${categoria.totalNiveles}",
            label = "Completados",
            isLocked = isLocked
        )
    }
}

// 🔹 OPTIMIZATION: Optimized info item
@Composable
private fun OptimizedInfoItem(value: String, label: String, isLocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isLocked) Color.White.copy(alpha = 0.6f) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isLocked) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

// 🔹 OPTIMIZATION: Optimized progress bar
@Composable
private fun OptimizedProgressBar(progress: Float, isLocked: Boolean) {
    val progressColor = remember(progress, isLocked) {
        when {
            isLocked -> Color.Gray.copy(alpha = 0.5f)
            progress >= 1f -> Color(0xFF00E676)
            else -> Color.White
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.2f))
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = progressColor,
            trackColor = Color.Transparent
        )
    }
}

// 🔹 OPTIMIZATION: Optimized progress percentage
@Composable
private fun OptimizedProgressPercentage(
    categoria: CategoriaConProgreso,
    isLocked: Boolean
) {
    val message = remember(categoria.dificultad, isLocked) {
        if (isLocked) {
            when (categoria.dificultad) {
                "Principiante" -> "Completa 50% del Tutorial"
                "Medio" -> "Completa 50% de Principiante"
                "Avanzado" -> "Completa 50% de Medio"
                "Experto" -> "Completa 50% de Avanzado"
                else -> "Completa la categoría anterior"
            }
        } else {
            "Progreso general"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isLocked) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )

        if (!isLocked) {
            Text(
                text = "${(categoria.progreso * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
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

    // 👇 CAMBIO IMPORTANTE: Usar el nuevo campo isUnlocked del ViewModel
    val isLocked = !categoria.isUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLocked,
                onClick = { onCategoriaClick(categoria.dificultad) }
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    // 👇 Cambiar el color de fondo si está bloqueada
                    brush = if (isLocked)
                        Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.7f), Color.DarkGray.copy(alpha = 0.7f)))
                    else
                        Brush.verticalGradient(colors),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header con icono y título
                CategoryHeader(
                    categoria = categoria,
                    isLocked = isLocked,
                    colors = if (isLocked) listOf(Color.Gray, Color.DarkGray) else colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Información de progreso
                ProgressInfo(
                    categoria = categoria,
                    isLocked = isLocked
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progreso
                ProgressBar(
                    progress = categoria.progreso,
                    isLocked = isLocked
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 👇 Porcentaje de completado con mensaje mejorado
                ProgressPercentage(
                    progress = categoria.progreso,
                    isLocked = isLocked,
                    categoria = categoria // 👈 Pasar la categoría completa
                )
            }
        }
    }
}

@Composable
fun CategoryHeader(
    categoria: CategoriaConProgreso,
    isLocked: Boolean,
    colors: List<Color>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Título con icono de dificultad
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        // 👇 Cambiar color del icono si está bloqueado
                        color = if (isLocked)
                            Color.Gray.copy(alpha = 0.5f)
                        else
                            colors[0].copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon: ImageVector = if (isLocked) {
                    Icons.Default.Lock // 👈 Icono de candado para categorías bloqueadas
                } else {
                    when (categoria.dificultad) {
                        "Tutorial" -> Icons.Filled.Star
                        "Principiante" -> Icons.Filled.Star
                        "Medio" -> Icons.Filled.Star
                        "Avanzado" -> Icons.Filled.Star
                        "Experto" -> Icons.Filled.Star
                        else -> Icons.Filled.Star
                    }
                }

                Icon(
                    imageVector = icon,
                    contentDescription = if (isLocked) "Bloqueado" else "Dificultad",
                    tint = if (isLocked) Color.White.copy(alpha = 0.7f) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = categoria.dificultad,
                style = MaterialTheme.typography.titleLarge,
                color = if (isLocked) Color.White.copy(alpha = 0.7f) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Icono de estado
        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Bloqueado",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        } else if (categoria.progreso >= 1f) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completado",
                tint = Color(0xFF00E676),
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
}

@Composable
fun ProgressInfo(
    categoria: CategoriaConProgreso,
    isLocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Niveles
        InfoItem(
            label = "Niveles",
            value = categoria.totalNiveles.toString(),
            isLocked = isLocked
        )

        // Puntos (ahora basado en 0-4 estrellas por nivel)
        InfoItem(
            label = "Estrellas",
            value = "${categoria.puntosObtenidos}/${categoria.puntosTotales}",
            isLocked = isLocked
        )

        // Niveles completados
        val nivelesCompletados = (categoria.totalNiveles * categoria.progreso).toInt()
        InfoItem(
            label = "Completados",
            value = "$nivelesCompletados/${categoria.totalNiveles}",
            isLocked = isLocked
        )
    }
}

@Composable
fun InfoItem(label: String, value: String, isLocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isLocked) Color.White.copy(alpha = 0.6f) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isLocked) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun ProgressBar(progress: Float, isLocked: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.2f))
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = when {
                isLocked -> Color.Gray.copy(alpha = 0.5f)
                progress >= 1f -> Color(0xFF00E676)
                else -> Color.White
            },
            trackColor = Color.Transparent
        )
    }
}

// 👇 FUNCIÓN ACTUALIZADA: Ahora recibe la categoría completa
@Composable
fun ProgressPercentage(progress: Float, isLocked: Boolean, categoria: CategoriaConProgreso) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isLocked) {
                // 👇 Mensaje específico para categorías bloqueadas
                when (categoria.dificultad) {
                    "Principiante" -> "Completa 50% del Tutorial"
                    "Medio" -> "Completa 50% de Principiante"
                    "Avanzado" -> "Completa 50% de Medio"
                    "Experto" -> "Completa 50% de Avanzado"
                    else -> "Completa la categoría anterior"
                }
            } else {
                "Progreso general"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isLocked) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )

        if (!isLocked) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

data class CategoriaConProgreso(
    val dificultad: String,
    val totalNiveles: Int,
    val puntosObtenidos: Int,
    val puntosTotales: Int,
    val isUnlocked: Boolean = true // 👈 Campo para el desbloqueo
) {
    val progreso: Float
        get() = if (puntosTotales > 0) puntosObtenidos.toFloat() / puntosTotales else 0f
}