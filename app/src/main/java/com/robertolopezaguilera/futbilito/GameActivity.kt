package com.robertolopezaguilera.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.ui.JuegoScreen

class GameActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var tiltX by mutableStateOf(0f)
    private var tiltY by mutableStateOf(0f)

    private lateinit var db: GameDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GameDatabase.getDatabase(this)

        // 🔹 Recibir el nivelId desde el Intent
        val nivelId = intent.getIntExtra("nivelId", 1)

        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            JuegoScreen(
                nivelId = nivelId,
                itemDao = db.itemDao(),
                obstaculoDao = db.obstaculoDao(),
                nivelDao = db.nivelDao(),
                onRestartNivel = {
                    // Si reinicias el nivel, relanza la misma Activity
                    finish()
                    startActivity(intent)
                },
                tiltX = tiltX,
                tiltY = tiltY
            )
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
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
