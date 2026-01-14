package com.robertolopezaguilera.futbilito.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robertolopezaguilera.futbilito.MusicManager
import com.robertolopezaguilera.futbilito.MusicService
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Item
import com.robertolopezaguilera.futbilito.data.ItemDao
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.Obstaculo
import com.robertolopezaguilera.futbilito.data.ObstaculoDao
import com.robertolopezaguilera.futbilito.data.Powers
import com.robertolopezaguilera.futbilito.data.PowersDao
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    gameViewModel: GameViewModel,
    tiendaViewModel: TiendaViewModel? = null
) {
    val context = LocalContext.current

//    // 🔹 CORRECCIÓN: Solo UN LaunchedEffect para controlar la música
//    LaunchedEffect(Unit) {
//        Log.d("JuegoScreen", "🎵 === INICIANDO MÚSICA DE JUEGO ===")
//
//        // Pequeño delay para asegurar que todo esté listo
//        delay(300)
//
//        // 🔹 SOLO UNA llamada a playGameMusic
//        MusicManager.playGameMusic(context)
//
//        Log.d("JuegoScreen", "🎵 === MÚSICA DE JUEGO INICIADA ===")
//    }
//
//    // 🔹 MEJORADO: DisposableEffect simplificado
//    DisposableEffect(Unit) {
//        onDispose {
//            Log.d("JuegoScreen", "🎵 === VOLVIENDO A MÚSICA DE MENÚ ===")
//            // Cambiar a música de menú inmediatamente
//            MusicManager.playMenuMusic(context)
//        }
//    }

    // Estados de carga separados para cada tipo de dato
    var nivel by remember { mutableStateOf<com.robertolopezaguilera.futbilito.data.Nivel?>(null) }
    var items by remember { mutableStateOf(emptyList<Item>()) }
    var obstaculos by remember { mutableStateOf(emptyList<Obstaculo>()) }
    var powers by remember { mutableStateOf(emptyList<Powers>()) }

    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var loadingMessage by remember { mutableStateOf("Iniciando nivel...") }

    // Cargar todos los datos de manera secuencial con progreso
    LaunchedEffect(nivelId) {
        isLoading = true
        loadingProgress = 0f

        try {
            // Paso 1: Cargar nivel básico
            loadingMessage = "Cargando configuración del nivel..."
            nivel = withContext(Dispatchers.IO) {
                nivelDao.getNivel(nivelId)
            }
            loadingProgress = 0.25f

            // Paso 2: Cargar items
            loadingMessage = "Cargando elementos a recolectar..."
            items = withContext(Dispatchers.IO) {
                itemDao.getItemsByNivel(nivelId).first()
            }
            loadingProgress = 0.5f

            // Paso 3: Cargar obstáculos (CRÍTICO)
            loadingMessage = "Cargando obstáculos..."
            obstaculos = withContext(Dispatchers.IO) {
                obstaculoDao.getObstaculosByNivel(nivelId).first()
            }
            loadingProgress = 0.75f

            // Paso 4: Cargar poderes
            loadingMessage = "Cargando power-ups..."
            powers = withContext(Dispatchers.IO) {
                powersDao.getPowersByNivel(nivelId).first()
            }
            loadingProgress = 1.0f

            // Pequeño delay para asegurar renderizado
            delay(100)

        } catch (e: Exception) {
            println("❌ Error cargando nivel $nivelId: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Mostrar pantalla de carga mientras se cargan los datos
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1B4A),
                            Color(0xFF172B6F)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFF4CAF50),
                    strokeWidth = 6.dp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = loadingMessage,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nivel $nivelId",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Debug info
                if (obstaculos.isNotEmpty()) {
                    Text(
                        text = "${obstaculos.size} obstáculos encontrados",
                        color = Color.Green.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
        return
    }

    // Verificar que tenemos todos los datos necesarios
    if (nivel == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B4A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Error cargando nivel",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRestartNivel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Reintentar")
                }
            }
        }
        return
    }

    // Pre-calcular borderObstacles con remember
    val borderObstacles = remember(nivelId) {
        listOf(
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 1000, largo = 20),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = 580,  ancho = 1000, largo = 20),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = -500, coordenadaY = -600, ancho = 20,  largo = 1200),
            com.robertolopezaguilera.futbilito.data.Obstaculo(nivelId = nivelId, coordenadaX = 500,  coordenadaY = -600, ancho = 20,  largo = 1200)
        )
    }

    // Estado del tiempo
    val tiempoRestante = remember { mutableStateOf(nivel!!.tiempo) }

    // Debug info
    LaunchedEffect(obstaculos.size, items.size, powers.size) {
        println("🎯 Nivel $nivelId cargado:")
        println("   - Obstáculos: ${obstaculos.size}")
        println("   - Items: ${items.size}")
        println("   - Powers: ${powers.size}")
        println("   - Border obstacles: ${borderObstacles.size}")
    }

    // 👇 NUEVO: Pasar el tiendaViewModel al MazeGame
    MazeGame(
        nivel = nivel!!,
        itemsFromDb = items,
        borderObstacles = borderObstacles,
        obstaclesFromDb = obstaculos,
        powersFromDb = powers,
        tiempoRestante = tiempoRestante,
        onTimeOut = {
            // Manejar tiempo agotado
        },
        onRestart = {
            tiempoRestante.value = nivel!!.tiempo
            onRestartNivel()
        },
        tiltX = tiltX,
        tiltY = tiltY,
        onLevelScored = { score ->
            gameViewModel.actualizarPuntuacion(nivelId, score)
        },
        onAddCoins = { coins ->
            gameViewModel.addMonedas(coins)
        },
        gameViewModel = gameViewModel,
        tiendaViewModel = tiendaViewModel
    )
}