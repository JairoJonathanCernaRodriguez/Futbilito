package com.robertolopezaguilera.futbilito.ui

import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.robertolopezaguilera.futbilito.GameEngine
import com.robertolopezaguilera.futbilito.GameState
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.ItemDao
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.data.ObstaculoDao
import com.robertolopezaguilera.futbilito.toGameObstacle
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel

@Composable
fun JuegoScreen(
    nivelId: Int,
    itemDao: ItemDao,
    obstaculoDao: ObstaculoDao,
    nivelDao: NivelDao,
    onRestartNivel: () -> Unit,
    tiltX: Float,
    tiltY: Float
) {
    val context = LocalContext.current

    // ViewModel para actualizar puntuación
    val gameViewModel: GameViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GameViewModel(GameDatabase.getDatabase(context)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val nivelState = produceState<Nivel?>(initialValue = null, key1 = nivelId) {
        value = nivelDao.getNivel(nivelId)
    }
    val nivel = nivelState.value

    val items by itemDao.getItemsByNivel(nivelId).collectAsState(initial = emptyList())
    val obstaculos by obstaculoDao.getObstaculosByNivel(nivelId).collectAsState(initial = emptyList())

    val borderObstacles = listOf(
        Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 1000, largo = 20),
        Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = 580,  ancho = 1000, largo = 20),
        Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 20,  largo = 1200),
        Obstaculo(nivelId = nivelId, coordenadaX = 500,  coordenadaY = -600, ancho = 20,  largo = 1200)
    )

    val tiempoRestante = remember { mutableStateOf(nivel?.tiempo ?: 60) }

    MazeGame(
        nivel = nivel,
        itemsFromDb = items,
        borderObstacles = borderObstacles,
        obstaclesFromDb = obstaculos,
        tiempoRestante = tiempoRestante,
        onTimeOut = {},
        onRestart = {
            tiempoRestante.value = nivel?.tiempo ?: 60
            onRestartNivel()
        },
        tiltX = tiltX,
        tiltY = tiltY,
        onLevelScored = { score ->
            gameViewModel.actualizarPuntuacion(nivelId, score)
        }
    )
}
