package com.robertolopezaguilera.futbilito.ui

import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.robertolopezaguilera.futbilito.GameEngine
import com.robertolopezaguilera.futbilito.GameState
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.ItemDao
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.data.ObstaculoDao
import com.robertolopezaguilera.futbilito.toGameObstacle

@Composable
fun JuegoScreen(
    nivelId: Int,
    itemDao: ItemDao,
    obstaculoDao: ObstaculoDao,
    nivelDao: NivelDao,
    onRestartNivel: () -> Unit,
    tiltX: Float,  // <-- sensor X
    tiltY: Float   // <-- sensor Y
) {
    val context = LocalContext.current
    val coinSound = remember { MediaPlayer.create(context, R.raw.coin_sound) }

    val nivelState = produceState<Nivel?>(initialValue = null, key1 = nivelId) {
        value = nivelDao.getNivel(nivelId)
    }
    val nivel = nivelState.value

    val items by itemDao.getItemsByNivel(nivelId).collectAsState(initial = emptyList())
    val obstaculos by obstaculoDao.getObstaculosByNivel(nivelId).collectAsState(initial = emptyList())

    val borderObstacles = listOf(
        Obstaculo(nivelId = nivelId, coordenadaX = -450, coordenadaY = -600, ancho = 900, largo = 20),
        Obstaculo(nivelId = nivelId, coordenadaX = -450, coordenadaY = 580,  ancho = 900, largo = 20),
        Obstaculo(nivelId = nivelId, coordenadaX = -450, coordenadaY = -600, ancho = 20,  largo = 1200),
        Obstaculo(nivelId = nivelId, coordenadaX = 430,  coordenadaY = -600, ancho = 20,  largo = 1200)
    )

    val tiempoRestante = remember { mutableStateOf(nivel?.tiempo ?: 60) }

    // 🔹 MazeGame recibe los valores del sensor
    MazeGame(
        nivel = nivel,
        itemsFromDb = items,
        obstaclesFromDb = (borderObstacles + obstaculos),
        tiempoRestante = tiempoRestante,
        onTimeOut = {},
        onRestart = {
            tiempoRestante.value = nivel?.tiempo ?: 60
            onRestartNivel()
        },
        tiltX = tiltX,   // <-- pasamos tiltX
        tiltY = tiltY    // <-- pasamos tiltY
    )
}
