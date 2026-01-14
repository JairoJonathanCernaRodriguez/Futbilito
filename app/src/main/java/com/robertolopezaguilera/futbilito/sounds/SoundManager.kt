package com.robertolopezaguilera.futbilito

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool
    private var soundMap: HashMap<String, Int> = HashMap()
    private val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

    // 🔹 CAMBIO: Solo volumen de efectos
    private var effectsVolume: Float = 1.0f

    companion object {
        private const val TAG = "SoundManager"
        private var instance: SoundManager? = null
        private const val PREF_EFFECTS_VOLUME = "effects_volume"
        private const val DEFAULT_EFFECTS_VOLUME = 1.0f

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

        loadSavedVolumes()
        loadSounds()

        Log.d(TAG, "SoundManager inicializado - Efectos: ${(effectsVolume * 100).toInt()}%")
    }

    private fun loadSounds() {
        try {
            soundMap["select"] = soundPool.load(context, R.raw.select_sound_arcade, 1)
            Log.d(TAG, "Sonido de selección cargado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar sonidos: ${e.message}")
        }
    }

    // 🔹 CAMBIO: Solo cargar volumen de efectos
    private fun loadSavedVolumes() {
        effectsVolume = prefs.getFloat(PREF_EFFECTS_VOLUME, DEFAULT_EFFECTS_VOLUME)
        Log.d(TAG, "Volumen de efectos cargado: $effectsVolume")
    }

    // 🔹 CAMBIO: Solo guardar volumen de efectos
    private fun saveEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat(PREF_EFFECTS_VOLUME, effectsVolume).apply()
        Log.d(TAG, "Volumen de efectos guardado: $effectsVolume")
    }

    fun playSelectSound() {
        try {
            val soundId = soundMap["select"]
            if (soundId != null && soundId != 0) {
                soundPool.play(soundId, effectsVolume, effectsVolume, 1, 0, 1.0f)
                Log.d(TAG, "Sonido de selección reproducido (volumen: ${(effectsVolume * 100).toInt()}%)")
            } else {
                Log.e(TAG, "Sonido de selección no encontrado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir sonido: ${e.message}")
        }
    }

    // 🔹 CAMBIO: Solo efectos
    fun setEffectsVolume(volume: Float) {
        saveEffectsVolume(volume)
        Log.d(TAG, "Volumen de efectos ajustado a: ${(effectsVolume * 100).toInt()}%")

        if (volume > 0f) {
            playSelectSound() // Demo del nuevo volumen
        }
    }

    // 🔹 CAMBIO: Solo efectos
    fun loadEffectsVolume(): Float {
        return prefs.getFloat(PREF_EFFECTS_VOLUME, DEFAULT_EFFECTS_VOLUME)
    }

    fun getEffectsVolume(): Float {
        return effectsVolume
    }

    fun toggleEffectsMute(): Float {
        val newVolume = if (effectsVolume > 0f) 0f else loadEffectsVolume()
        setEffectsVolume(newVolume)
        return newVolume
    }

    fun isEffectsMuted(): Boolean {
        return effectsVolume == 0f
    }

    fun release() {
        soundPool.release()
        instance = null
    }
}