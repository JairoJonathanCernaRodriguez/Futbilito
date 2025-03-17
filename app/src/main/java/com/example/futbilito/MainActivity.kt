package com.example.futbilito

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var _x by mutableStateOf(0f)
    private var _y by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            FutbolitoGame(_x, _y)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val ax = it.values[0]
            val ay = it.values[1]

            // Ajustar la posición en base al sensor (invertido porque el acelerómetro da valores contrarios)
            _x = max(-450f, min(450f, _x - ax * 5))
            _y = max(-700f, min(700f, _y + ay * 5))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun FutbolitoGame(x: Float, y: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.cancha_futbol),
            contentDescription = "Cancha de fútbol",
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val ballRadius = 7.dp.toPx()
            val goalWidth = ballRadius * 2
            val goalHeight = ballRadius * 2

            // Dibujar las porterías en los márgenes de la pelota
            drawRect(Color.White, topLeft = Offset((size.width - goalWidth) / 2, 0f), size = Size(goalWidth, goalHeight))
            drawRect(Color.White, topLeft = Offset((size.width - goalWidth) / 2, size.height - goalHeight), size = Size(goalWidth, goalHeight))

            // Dibujar la pelota
            drawCircle(
                color = Color.Black,
                radius = ballRadius, // Pelota de tamaño 7.dp
                center = Offset(size.width / 2 + x, size.height / 2 + y)
            )
        }
    }
}
