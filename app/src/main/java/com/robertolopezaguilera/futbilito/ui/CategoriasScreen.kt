package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel

@Composable
fun CategoriasScreen(
    viewModel: NivelViewModel,
    onCategoriaClick: (String) -> Unit
) {
    val categorias by viewModel.categoriasConProgreso.collectAsState()

    LazyColumn {
        items(categorias) { categoria ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onCategoriaClick(categoria.dificultad) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = categoria.dificultad,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Niveles: ${categoria.totalNiveles}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { categoria.progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(categoria.progreso * 100).toInt()}% completado",
                        style = MaterialTheme.typography.bodySmall
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
