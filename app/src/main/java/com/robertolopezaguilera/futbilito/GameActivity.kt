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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.ui.JuegoScreen
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModelFactory
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var tiltX by mutableStateOf(0f)
    private var tiltY by mutableStateOf(0f)
    private lateinit var db: GameDatabase

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory(GameDatabase.getDatabase(this))
    }

    private lateinit var tiendaViewModel: TiendaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)
        tiendaViewModel = ViewModelProvider(
            this,
            TiendaViewModelFactory(gameViewModel, db.tiendaDao())
        )[TiendaViewModel::class.java]

        val nivelId = intent.getIntExtra("nivelId", 1)
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gameViewModel.loadUsuario()

        // 🔹 CAMBIO: Usar playGameMusic en lugar de playGameMusicSafely
        Log.d("GameActivity", "🎮 Iniciando GameActivity - cambiando a música de JUEGO")
        MusicManager.playGameMusic(this)

        setContent {
            val context = LocalContext.current
            val usuario by gameViewModel.usuario.collectAsState()

            LaunchedEffect(usuario) {
                if (usuario != null) {
                    println("👤 Usuario cargado en GameActivity: ${usuario!!.nombre}, Monedas: ${usuario!!.monedas}")
                }
            }

            // 🔹 EFECTO: Asegurar música de juego mientras estemos en esta activity
            LaunchedEffect(Unit) {
                delay(100)
                MusicManager.playGameMusic(context)
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
                gameViewModel = gameViewModel,
                tiendaViewModel = tiendaViewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("GameActivity", "🎮 GameActivity en primer plano")
        MusicManager.notifyAppInForeground(this)

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gameViewModel.loadUsuario()

        // 🔹 CRÍTICO: Asegurar música de juego al resumir
        lifecycleScope.launch {
            delay(150)
            if (!isFinishing) {
                Log.d("GameActivity", "🎵 Verificando música de JUEGO en onResume")
                MusicManager.playGameMusic(this@GameActivity)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("GameActivity", "🎮 GameActivity en pausa")
        sensorManager.unregisterListener(this)
        // NO pausar música aquí - dejar que MainActivity la restaure
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GameActivity", "🗑️ GameActivity destruida")
    }

    override fun onBackPressed() {
        Log.d("GameActivity", "🔙 Volviendo al menú desde juego")

        // 🔹 CRÍTICO: Cambiar a música de MENÚ ANTES de terminar la actividad
        MusicManager.ensureMenuMusic(this)

        super.onBackPressed()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d("GameActivity", "👤 Usuario saliendo de GameActivity")
        MusicManager.notifyAppInBackground(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            tiltX = -it.values[0]
            tiltY = it.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}