package com.robertolopezaguilera.futbilito

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.robertolopezaguilera.futbilito.data.*
import com.robertolopezaguilera.futbilito.niveles.*
import com.robertolopezaguilera.futbilito.ui.AppNavigation
import com.robertolopezaguilera.utbilito.niveles.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : ComponentActivity() {

    private lateinit var db: GameDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)

        lifecycleScope.launch { inicializarNiveles() }

        // 🔹 INICIAR EL SERVICIO DE MÚSICA CON UN PEQUEÑO DELAY
        lifecycleScope.launch {
            startMenuMusicService()
        }

        setContent {
            var isLoaded by remember { mutableStateOf(false) }
            var usuario by remember { mutableStateOf<Usuario?>(null) }

            // 🔹 Cargar usuario 1 sola vez
            LaunchedEffect(Unit) {
                usuario = withContext(Dispatchers.IO) { db.usuarioDao().getUsuario() }
                isLoaded = true
            }

            if (!isLoaded) {
                LoadingScreen()
            } else {
                AppNavigation(
                    startDestination = if (usuario == null) "registro" else "main",
                    db = db
                )
            }
        }
    }

    // 🔹 CAMBIO: Método específico para música de menú
    private fun startMenuMusicService() {
        try {
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("action", "play")
            intent.putExtra("track", "menu") // 🔹 Especificar track de menú
            startService(intent)
            Log.d("MainActivity", "Servicio de música de menú iniciado")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar servicio de música de menú: ${e.message}")
        }
    }

    // 🔹 PAUSAR MÚSICA CUANDO LA APP ENTRA EN SEGUNDO PLANO
    override fun onPause() {
        super.onPause()
        pauseMusic()
    }

    // 🔹 REANUDAR MÚSICA CUANDO LA APP VUELVE AL PRIMER PLANO
    override fun onResume() {
        super.onResume()
        resumeMusic()
    }

    private fun pauseMusic() {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("action", "pause")
        startService(intent)
    }

    private fun resumeMusic() {
        try {
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("action", "play")
            intent.putExtra("track", "menu") // 🔹 Especificar track de menú
            startService(intent)
            Log.d("MainActivity", "Música de menú reanudada")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al reanudar música de menú: ${e.message}")
        }
    }

    // 🔹 DETENER MÚSICA CUANDO LA APP SE CIERRA
    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }

    private fun stopMusic() {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("action", "stop")
        startService(intent)
    }

    private suspend fun inicializarNiveles() {
        withContext(Dispatchers.IO) {
            val dao = db.nivelDao()
            if (dao.getAllNiveles().isEmpty()) {
                val niveles = mutableListOf<Nivel>()
                var id = 1
                repeat(10) { niveles.add(Nivel(id++, 60, 0,
                    "Tutorial")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 0,
                    "Principiante")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 0,
                    "Medio")) }
                repeat(20) { niveles.add(Nivel(id++, 60, 0,
                    "Avanzado")) }
                repeat(10) { niveles.add(Nivel(id++, 60, 0,
                    "Experto")) }
                dao.insertNiveles(niveles)

                val daoItems = db.itemDao()
                //Tutorial
                daoItems.insertListItem(itemsNivel1)
                daoItems.insertListItem(itemsNivel2)
                daoItems.insertListItem(itemsNivel3)
                daoItems.insertListItem(itemsNivel4)
                daoItems.insertListItem(itemsNivel5)
                daoItems.insertListItem(itemsNivel6)
                daoItems.insertListItem(itemsNivel7)
                daoItems.insertListItem(itemsNivel8)
                daoItems.insertListItem(itemsNivel9)
                daoItems.insertListItem(itemsNivel10)
                //
                val daoObstaculos = db.obstaculoDao()
                daoObstaculos.insertListObstculo(obstaclesNivel1)
                daoObstaculos.insertListObstculo(obstaclesNivel2)
                daoObstaculos.insertListObstculo(obstaclesNivel3)
                daoObstaculos.insertListObstculo(obstaclesNivel4)
                daoObstaculos.insertListObstculo(obstaclesNivel5)
                daoObstaculos.insertListObstculo(obstaclesNivel6)
                daoObstaculos.insertListObstculo(obstaclesNivel7)
                daoObstaculos.insertListObstculo(obstaclesNivel8)
                daoObstaculos.insertListObstculo(obstaclesNivel9)
                daoObstaculos.insertListObstculo(obstaclesNivel10)
                //
                val powersDao = db.powersDao()
                val poderesNivel1 = listOf(
                    Powers(coordenadaX = 100, coordenadaY = -100, nivelId = 1, tipo = "speed_boost"),
                    Powers(coordenadaX = -150, coordenadaY = 150, nivelId = 1, tipo = "ghost_mode")
                )
                powersDao.insertarPower(Powers(coordenadaX = 100, coordenadaY = -100, nivelId = 1,
                    tipo = "speed_boost"))
                //Principiate

                daoItems.insertListItem(itemsNivel11)
                daoItems.insertListItem(itemsNivel12)
                daoItems.insertListItem(itemsNivel13)
                daoItems.insertListItem(itemsNivel14)
                daoItems.insertListItem(itemsNivel15)
                daoItems.insertListItem(itemsNivel16)
                daoItems.insertListItem(itemsNivel17)
                daoItems.insertListItem(itemsNivel18)
                daoItems.insertListItem(itemsNivel19)
                //
                daoObstaculos.insertListObstculo(obstaclesNivel11)
                daoObstaculos.insertListObstculo(obstaclesNivel12)
                powersDao.insertarPower(Powers(coordenadaX = -380, coordenadaY = -560, nivelId = 12,
                    tipo = "speed_boost"))
                daoObstaculos.insertListObstculo(obstaclesNivel13)
                daoObstaculos.insertListObstculo(obstaclesNivel14)
                daoObstaculos.insertListObstculo(obstaclesNivel15)
                daoObstaculos.insertListObstculo(obstaclesNivel16)
                daoObstaculos.insertListObstculo(obstaclesNivel17)
                powersDao.insertarPower(Powers(coordenadaX = 300, coordenadaY = -80, nivelId = 17,
                    tipo = "speed_boost"))
                daoObstaculos.insertListObstculo(obstaclesNivel18)
                powersDao.insertarPower(Powers(coordenadaX = -300, coordenadaY = -200, nivelId = 18,
                    tipo = "speed_boost"))
                daoObstaculos.insertListObstculo(obstaclesNivel19)
                powersDao.insertarPower(Powers(coordenadaX = 380, coordenadaY = -440, nivelId = 19,
                    tipo = "ghost_mode"))

                //Dificil


            }
        }
    }
}

// --- UI simple de carga ---
@Composable private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.size(16.dp))
            Text("Cargando...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
