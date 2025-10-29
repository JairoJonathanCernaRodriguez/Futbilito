package com.robertolopezaguilera.futbilito

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool
    private var soundMap: HashMap<String, Int> = HashMap()

    // 🔹 NUEVO: SharedPreferences para persistencia
    private val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

    // 🔹 NUEVO: Variables con valores por defecto
    private var effectsVolume: Float = 1.0f
    private var musicVolume: Float = 0.7f

    companion object {
        private const val TAG = "SoundManager"
        private var instance: SoundManager? = null

        // 🔹 NUEVO: Claves para SharedPreferences
        private const val PREF_EFFECTS_VOLUME = "effects_volume"
        private const val PREF_MUSIC_VOLUME = "music_volume"
        private const val DEFAULT_EFFECTS_VOLUME = 1.0f
        private const val DEFAULT_MUSIC_VOLUME = 0.7f

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

        // 🔹 CAMBIO: Cargar volúmenes guardados antes de cargar sonidos
        loadSavedVolumes()

        // Cargar sonidos
        loadSounds()

        Log.d(TAG, "SoundManager inicializado - Efectos: ${(effectsVolume * 100).toInt()}%, Música: ${(musicVolume * 100).toInt()}%")
    }

    private fun loadSounds() {
        try {
            soundMap["select"] = soundPool.load(context, R.raw.select_sound_arcade, 1)
            Log.d(TAG, "Sonido de selección cargado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar sonidos: ${e.message}")
        }
    }

    // 🔹 NUEVO: Cargar volúmenes guardados
    private fun loadSavedVolumes() {
        effectsVolume = prefs.getFloat(PREF_EFFECTS_VOLUME, DEFAULT_EFFECTS_VOLUME)
        musicVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
        Log.d(TAG, "Volúmenes cargados - Efectos: $effectsVolume, Música: $musicVolume")
    }

    // 🔹 NUEVO: Guardar volumen de efectos
    private fun saveEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat(PREF_EFFECTS_VOLUME, effectsVolume).apply()
        Log.d(TAG, "Volumen de efectos guardado: $effectsVolume")
    }

    // 🔹 NUEVO: Guardar volumen de música
    fun saveMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat(PREF_MUSIC_VOLUME, musicVolume).apply()
        Log.d(TAG, "Volumen de música guardado: $musicVolume")
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

    // 🔹 MEJORADO: Cambiar volumen de efectos con persistencia
    fun setEffectsVolume(volume: Float) {
        saveEffectsVolume(volume)
        Log.d(TAG, "Volumen de efectos ajustado a: ${(effectsVolume * 100).toInt()}%")

        // 🔹 OPCIONAL: Reproducir sonido de prueba al cambiar volumen (si no está en mute)
        if (volume > 0f) {
            playSelectSound() // Demo del nuevo volumen
        }
    }

    // 🔹 NUEVO: Obtener volumen de música
    fun getMusicVolume(): Float {
        return musicVolume
    }

    // 🔹 NUEVO: Cargar volumen de música (para inicialización)
    fun loadMusicVolume(): Float {
        return prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
    }

    // 🔹 NUEVO: Cargar volumen de efectos (para inicialización)
    fun loadEffectsVolume(): Float {
        return prefs.getFloat(PREF_EFFECTS_VOLUME, DEFAULT_EFFECTS_VOLUME)
    }

    // 🔹 Obtener volumen actual de efectos
    fun getEffectsVolume(): Float {
        return effectsVolume
    }

    // 🔹 MEJORADO: Silenciar/activar efectos con persistencia
    fun toggleEffectsMute(): Float {
        val newVolume = if (effectsVolume > 0f) 0f else loadEffectsVolume()
        if (newVolume > 0f && effectsVolume == 0f) {
            // Si estamos reactivando, usar el último volumen guardado
            setEffectsVolume(newVolume)
        } else {
            // Si estamos silenciando, guardar 0
            setEffectsVolume(newVolume)
        }
        return newVolume
    }

    // 🔹 NUEVO: Resetear a valores por defecto
    fun resetToDefaults() {
        setEffectsVolume(DEFAULT_EFFECTS_VOLUME)
        saveMusicVolume(DEFAULT_MUSIC_VOLUME)
        Log.d(TAG, "Volúmenes reseteados a valores por defecto")
    }

    // 🔹 NUEVO: Verificar si los efectos están silenciados
    fun isEffectsMuted(): Boolean {
        return effectsVolume == 0f
    }

    // 🔹 NUEVO: Verificar si la música está silenciada
    fun isMusicMuted(): Boolean {
        return musicVolume == 0f
    }

    fun release() {
        soundPool.release()
        instance = null
    }
}