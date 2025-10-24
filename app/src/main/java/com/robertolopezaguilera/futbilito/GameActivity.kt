package com.robertolopezaguilera.futbilito

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.ui.JuegoScreen
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModelFactory

class GameActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var tiltX by mutableStateOf(0f)
    private var tiltY by mutableStateOf(0f)

    private lateinit var db: GameDatabase

    // 👇 Crear el ViewModel usando viewModels delegate
    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory(GameDatabase.getDatabase(this))
    }

    // 🔹 NUEVO: Variable para controlar si ya se inició la música
    private var musicStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)

        // 🔹 Recibir el nivelId desde el Intent
        val nivelId = intent.getIntExtra("nivelId", 1)

        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 👇 CARGAR USUARIO AL INICIAR LA ACTIVITY
        gameViewModel.loadUsuario()

        // 🔹 CAMBIO: Solo reproducir música de juego si no se ha iniciado antes
        if (!musicStarted) {
            playGameMusic()
            musicStarted = true
        }

        setContent {
            // 👇 Observar el estado de carga del usuario
            val usuario by gameViewModel.usuario.collectAsState()

            LaunchedEffect(usuario) {
                if (usuario != null) {
                    println("👤 Usuario cargado en GameActivity: ${usuario!!.nombre}, Monedas: ${usuario!!.monedas}")
                }
            }

            JuegoScreen(
                nivelId = nivelId,
                itemDao = db.itemDao(),
                obstaculoDao = db.obstaculoDao(),
                nivelDao = db.nivelDao(),
                powersDao = db.powersDao(),
                onRestartNivel = {
                    finish()
                    startActivity(intent)
                },
                tiltX = tiltX,
                tiltY = tiltY,
                gameViewModel = gameViewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        // 👇 Recargar usuario al resumir la activity por si acaso
        gameViewModel.loadUsuario()

        // 🔹 CAMBIO: Solo reanudar si la música ya estaba iniciada
        // No llamar a resumeGameMusic() aquí para evitar reinicios
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

        // 🔹 OPCIONAL: Pausar música cuando el juego está en pausa
        // Si quieres que la música continue incluso cuando el juego está en pausa,
        // comenta esta línea
        // pauseMusic()
    }

    override fun onBackPressed() {
        // 🔹 NUEVO: Cambiar a música de menú al volver
        playMenuMusic()
        musicStarted = false // 🔹 Resetear para la próxima vez
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🔹 NO detener la música aquí, solo cuando la app se cierre completamente
        // La música debe continuar entre actividades
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            tiltX = -it.values[0]
            tiltY = it.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun playGameMusic() {
        try {
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("action", "play")
            intent.putExtra("track", "game") // 🔹 Especificar track de juego
            startService(intent)
            Log.d("GameActivity", "Música de juego iniciada")
        } catch (e: Exception) {
            Log.e("GameActivity", "Error al iniciar música de juego: ${e.message}")
        }
    }

    // 🔹 ELIMINAR: Este método ya no es necesario
    // private fun resumeGameMusic() { ... }

    // 🔹 NUEVO: Método para cambiar a música de menú
    private fun playMenuMusic() {
        try {
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("action", "play")
            intent.putExtra("track", "menu") // 🔹 Especificar track de menú
            startService(intent)
            Log.d("GameActivity", "Cambiando a música de menú")
        } catch (e: Exception) {
            Log.e("GameActivity", "Error al cambiar a música de menú: ${e.message}")
        }
    }

    private fun pauseMusic() {
        try {
            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("action", "pause")
            startService(intent)
            Log.d("GameActivity", "Música pausada desde GameActivity")
        } catch (e: Exception) {
            Log.e("GameActivity", "Error al pausar música: ${e.message}")
        }
    }
}