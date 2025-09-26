package com.robertolopezaguilera.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)

        // 🔹 Recibir el nivelId desde el Intent
        val nivelId = intent.getIntExtra("nivelId", 1)

        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 👇 CARGAR USUARIO AL INICIAR LA ACTIVITY
        gameViewModel.loadUsuario()

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
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            tiltX = -it.values[0]
            tiltY = it.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}