package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel

@Composable
fun NivelesScreen(
    viewModel: NivelViewModel,
    categoria: String,
    onNivelClick: (Int, Int) -> Unit // Recibe nivelId y tiempo
) {
    val niveles by viewModel.getNivelesPorCategoria(categoria).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text(
            text = "Categoría: $categoria",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn {
            items(niveles) { nivel ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            onNivelClick(nivel.id, nivel.tiempo) // <-- Aquí mandamos al juego
                        }
                ) {
                    Text(
                        text = "Nivel ${nivel.id} - Tiempo: ${nivel.tiempo}s",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
