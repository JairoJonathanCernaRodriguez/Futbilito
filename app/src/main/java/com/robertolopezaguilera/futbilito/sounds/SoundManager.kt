package com.robertolopezaguilera.futbilito

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool
    private var soundMap: HashMap<String, Int> = HashMap()

    // 🔹 NUEVO: Variable para controlar el volumen global de efectos
    private var effectsVolume: Float = 1.0f

    companion object {
        private const val TAG = "SoundManager"
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargar sonidos
        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundMap["select"] = soundPool.load(context, R.raw.select_sound_arcade, 1)
            Log.d(TAG, "Sonido de selección cargado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar sonidos: ${e.message}")
        }
    }

    fun playSelectSound() {
        try {
            val soundId = soundMap["select"]
            if (soundId != null && soundId != 0) {
                // 🔹 CAMBIO: Usar effectsVolume en lugar de 1.0f
                soundPool.play(soundId, effectsVolume, effectsVolume, 1, 0, 1.0f)
                Log.d(TAG, "Sonido de selección reproducido (volumen: ${(effectsVolume * 100).toInt()}%)")
            } else {
                Log.e(TAG, "Sonido de selección no encontrado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir sonido: ${e.message}")
        }
    }

    // 🔹 NUEVA FUNCIÓN: Cambiar volumen de efectos
    fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        Log.d(TAG, "Volumen de efectos ajustado a: ${(effectsVolume * 100).toInt()}%")

        // 🔹 OPCIONAL: Reproducir sonido de prueba al cambiar volumen (si no está en mute)
        if (volume > 0f) {
            playSelectSound() // Demo del nuevo volumen
        }
    }

    // 🔹 NUEVA FUNCIÓN: Obtener volumen actual
    fun getEffectsVolume(): Float {
        return effectsVolume
    }

    // 🔹 NUEVA FUNCIÓN: Silenciar/activar efectos
    fun toggleEffectsMute(): Float {
        val newVolume = if (effectsVolume > 0f) 0f else 1f
        setEffectsVolume(newVolume)
        return newVolume
    }

    fun release() {
        soundPool.release()
        instance = null
    }
}