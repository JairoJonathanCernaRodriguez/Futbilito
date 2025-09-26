package com.robertolopezaguilera.futbilito.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.ItemDao
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.ObstaculoDao
import com.robertolopezaguilera.futbilito.data.PowersDao
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun JuegoScreen(
    nivelId: Int,
    itemDao: ItemDao,
    obstaculoDao: ObstaculoDao,
    nivelDao: NivelDao,
    powersDao: PowersDao,
    onRestartNivel: () -> Unit,
    tiltX: Float,
    tiltY: Float,
    gameViewModel: GameViewModel // 👈 Recibir el ViewModel desde fuera
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // 👇 Cargar nivel de forma asíncrona (no es Flow)
    val nivelState = produceState<com.robertolopezaguilera.futbilito.data.Nivel?>(initialValue = null, key1 = nivelId) {
        value = withContext(Dispatchers.IO) {
            nivelDao.getNivel(nivelId)
        }
    }

    // 👇 Usar collectAsState() para los Flows
    val items by itemDao.getItemsByNivel(nivelId).collectAsState(initial = emptyList())
    val obstaculos by obstaculoDao.getObstaculosByNivel(nivelId).collectAsState(initial = emptyList())
    val powers by powersDao.getPowersByNivel(nivelId).collectAsState(initial = emptyList())

    // 👇 Verificar si todos los datos están cargados
    LaunchedEffect(nivelState.value, items, obstaculos, powers) {
        if (nivelState.value != null) {
            // Pequeño delay para asegurar que todo esté renderizado
            kotlinx.coroutines.delay(50)
            isLoading = false
        }
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    val nivel = nivelState.value

    // 👇 Pre-calcular borderObstacles con remember
    val borderObstacles = remember(nivelId) {
        listOf(
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 1000, largo = 20),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = 580,  ancho = 1000, largo = 20),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 20,  largo = 1200),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = 500,  coordenadaY = -600, ancho = 20,  largo = 1200)
        )
    }

    // Estado del tiempo
    val tiempoRestante = remember { mutableStateOf(nivel?.tiempo ?: 60) }

    // 👇 Actualiza el tiempo inicial cuando llega el nivel
    LaunchedEffect(nivel) {
        nivel?.let {
            tiempoRestante.value = it.tiempo
        }
    }

    MazeGame(
        nivel = nivel,
        itemsFromDb = items,
        borderObstacles = borderObstacles,
        obstaclesFromDb = obstaculos,
        powersFromDb = powers,
        tiempoRestante = tiempoRestante,
        onTimeOut = {
            // Manejar tiempo agotado
        },
        onRestart = {
            tiempoRestante.value = nivel?.tiempo ?: 60
            onRestartNivel()
        },
        tiltX = tiltX,
        tiltY = tiltY,
        onLevelScored = { score ->
            gameViewModel.actualizarPuntuacion(nivelId, score)
        },
        // 👇 NUEVO: Pasar la función para agregar monedas
        onAddCoins = { coins ->
            gameViewModel.addMonedas(coins)
        },
        gameViewModel = gameViewModel
    )
}
