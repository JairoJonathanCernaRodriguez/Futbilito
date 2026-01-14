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
import androidx.navigation.findNavController
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
    private var musicStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)
        lifecycleScope.launch { inicializarNiveles() }

        // 🔹 INICIAR SERVICIO DE MÚSICA UNA SOLA VEZ
        if (!musicStarted) {
            Log.d("MainActivity", "🚀 INICIANDO SERVICIO DE MÚSICA MENU")
            MusicManager.startMenuMusic(this)
            musicStarted = true
        }

        setContent {
            var isLoaded by remember { mutableStateOf(false) }
            var usuario by remember { mutableStateOf<Usuario?>(null) }

            LaunchedEffect(Unit) {
                usuario = withContext(Dispatchers.IO) { db.usuarioDao().getUsuario() }
                isLoaded = true
            }

            if (!isLoaded) {
                LoadingScreen()
            } else {
                AppNavigation(
                    startDestination = if (usuario == null) "registro" else "main",
                    db = db,
                    onGameActivityLaunched = {
                        Log.d("MainActivity", "🎮 GameActivity lanzada")
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "🏠 MainActivity en primer plano")
        MusicManager.notifyAppInForeground(this)

        // 🔹 SOLO REANUDAR SI ESTÁ PAUSADA, NO REINICIAR
        lifecycleScope.launch {
            delay(100)
            if (musicStarted) {
                Log.d("MainActivity", "🔄 Reanudando música si está pausada")
                MusicManager.resumeMusic(this@MainActivity)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d("MainActivity", "📱 App en segundo plano")
        MusicManager.notifyAppInBackground(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "🗑️ MainActivity destruida")
        if (isFinishing) {
            Log.d("MainActivity", "🚨 Cerrando app - deteniendo música")
            MusicManager.stopMusic(this)
            musicStarted = false
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            Log.d("MainActivity", "🔙 Saliendo de la app")
            MusicManager.stopMusic(this)
            musicStarted = false
        }
        super.onBackPressed()
    }

    private suspend fun inicializarNiveles() {
        withContext(Dispatchers.IO) {
            val dao = db.nivelDao()
            if (dao.getAllNiveles().isEmpty()) {
                val niveles = mutableListOf<Nivel>()
                var id = 1
                repeat(10) { niveles.add(Nivel(id++, 60, 4, "Tutorial")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 4, "Principiante")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 4, "Medio")) }
                repeat(20) { niveles.add(Nivel(id++, 60, 0, "Avanzado")) }
                repeat(10) { niveles.add(Nivel(id++, 60, 0, "Experto")) }
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
                //Principiante

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
                powersDao.insertarPower(Powers(coordenadaX = 380, coordenadaY = -440, nivelId = 13,
                    tipo = "ghost_mode"))
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