package com.robertolopezaguilera.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.robertolopezaguilera.futbilito.ui.AppNavigation
import com.robertolopezaguilera.utbilito.niveles.obstaclesNivel1
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
                repeat(10) { niveles.add(Nivel(id++, 45, 0, "Tutorial")) }
                repeat(30) { niveles.add(Nivel(id++, 45, 0, "Principiante")) }
                repeat(30) { niveles.add(Nivel(id++, 45, 0, "Medio")) }
                repeat(20) { niveles.add(Nivel(id++, 45, 0, "Avanzado")) }
                repeat(10) { niveles.add(Nivel(id++, 45, 0, "Experto")) }
                dao.insertNiveles(niveles)

                val daoItems = db.itemDao()
                daoItems.insertItem(Item(nivelId = 1, coordenadaX = -350, coordenadaY = -540))
                val daoObstaculos = db.obstaculoDao()
                daoObstaculos.insertListObstculo(obstaclesNivel1)
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
