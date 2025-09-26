package com.robertolopezaguilera.futbilito

import android.os.Bundle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var db: GameDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)

        lifecycleScope.launch { inicializarNiveles() }

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
                    startDestination = if (usuario == null) "registro" else "categorias",
                    db = db
                )
            }
        }
    }

    private suspend fun inicializarNiveles() {
        withContext(Dispatchers.IO) {
            val dao = db.nivelDao()
            if (dao.getAllNiveles().isEmpty()) {
                val niveles = mutableListOf<Nivel>()
                var id = 1
                repeat(10) { niveles.add(Nivel(id++, 60, 0, "Tutorial")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 0, "Principiante")) }
                repeat(30) { niveles.add(Nivel(id++, 60, 0, "Medio")) }
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
                powersDao.insertarPower(Powers(coordenadaX = 100, coordenadaY = -100, nivelId = 1, tipo = "speed_boost"))
                //Principiate

                daoItems.insertListItem(itemsNivel11)
                daoItems.insertListItem(itemsNivel12)
                daoItems.insertListItem(itemsNivel13)
                daoItems.insertListItem(itemsNivel14)
                //
                daoObstaculos.insertListObstculo(obstaclesNivel11)
                daoObstaculos.insertListObstculo(obstaclesNivel12)
                daoObstaculos.insertListObstculo(obstaclesNivel13)
                daoObstaculos.insertListObstculo(obstaclesNivel14)
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
