package com.robertolopezaguilera.futbilito

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private var shouldPlay = true
    private var isPausedByUser = false
    private var currentTrack: MusicTrack = MusicTrack.MENU
    private var currentVolume: Float = 0.7f // 🔹 NUEVO: Variable para trackear volumen actual

    // 🔹 NUEVO: SharedPreferences para cargar volumen guardado
    private lateinit var prefs: android.content.SharedPreferences

    enum class MusicTrack(val resourceId: Int) {
        MENU(R.raw.game_music_loop_14),
        GAME(R.raw.game_music_loop_19)
    }

    companion object {
        private const val TAG = "MusicService"
        private const val PREF_MUSIC_VOLUME = "music_volume"
        private const val DEFAULT_MUSIC_VOLUME = 0.7f
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio de música creado")

        // 🔹 NUEVO: Inicializar SharedPreferences
        prefs = getSharedPreferences("audio_settings", android.content.Context.MODE_PRIVATE)

        // 🔹 NUEVO: Cargar volumen guardado al crear el servicio
        currentVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
        Log.d(TAG, "Volumen cargado al crear servicio: $currentVolume")
    }

    private fun initializeMediaPlayer(track: MusicTrack = MusicTrack.MENU) {
        try {
            // Liberar cualquier instancia previa
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer()
            currentTrack = track

            mediaPlayer?.apply {
                // Configurar el data source con el track específico
                val afd = resources.openRawResourceFd(track.resourceId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                // 🔹 NUEVO: Aplicar volumen guardado inmediatamente después de preparar
                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer preparado correctamente para: $track")
                    isPrepared = true

                    // 🔹 APLICAR VOLUMEN GUARDADO
                    mp.setVolume(currentVolume, currentVolume)
                    Log.d(TAG, "Volumen aplicado al MediaPlayer: $currentVolume")

                    if (shouldPlay && !isPausedByUser) {
                        mp.start()
                        Log.d(TAG, "Música reproduciéndose: $track con volumen: ${(currentVolume * 100).toInt()}%")
                    }
                }

                setOnCompletionListener { mp ->
                    Log.d(TAG, "Canción completada: $track, reiniciando inmediatamente...")
                    if (shouldPlay && isPrepared && !isPausedByUser) {
                        mp.seekTo(0)
                        mp.start()
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Error en MediaPlayer para $track - what: $what, extra: $extra")
                    android.os.Handler(mainLooper).postDelayed({
                        resetMediaPlayer()
                    }, 1000)
                    true
                }

                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar MediaPlayer para $track: ${e.message}")
            e.printStackTrace()
            resetMediaPlayer()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand recibido: ${intent?.getStringExtra("action")}, track: ${intent?.getStringExtra("track")}")

        val requestedTrack = when (intent?.getStringExtra("track")) {
            "game" -> MusicTrack.GAME
            "menu" -> MusicTrack.MENU
            else -> MusicTrack.MENU
        }

        when (intent?.getStringExtra("action")) {
            "play" -> {
                // 🔹 CAMBIO: Cargar volumen actualizado antes de reproducir
                currentVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)

                if (currentTrack != requestedTrack || mediaPlayer == null) {
                    Log.d(TAG, "Cambiando track de $currentTrack a $requestedTrack")
                    initializeMediaPlayer(requestedTrack)
                } else {
                    shouldPlay = true
                    isPausedByUser = false
                    playMusic()
                }
            }
            "pause" -> {
                isPausedByUser = true
                pauseMusic()
            }
            "stop" -> {
                shouldPlay = false
                isPausedByUser = false
                stopMusic()
            }
            "set_volume" -> {
                val volume = intent.getFloatExtra("volume", 0.7f)
                setVolume(volume)
                // 🔹 NUEVO: Guardar inmediatamente en SharedPreferences
                prefs.edit().putFloat(PREF_MUSIC_VOLUME, volume).apply()
                Log.d(TAG, "Volumen guardado en SharedPreferences: $volume")
            }
            else -> {
                // 🔹 CAMBIO: Cargar volumen actualizado para acción por defecto
                currentVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)

                if (currentTrack != requestedTrack || mediaPlayer == null) {
                    initializeMediaPlayer(requestedTrack)
                } else {
                    shouldPlay = true
                    isPausedByUser = false
                    playMusic()
                }
            }
        }
        return START_STICKY
    }

    private fun setVolume(volume: Float) {
        try {
            currentVolume = volume.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(currentVolume, currentVolume)
            Log.d(TAG, "Volumen de música ajustado a: ${(currentVolume * 100).toInt()}%")

            // 🔹 NUEVO: Si el volumen es 0, pausar; si es > 0 y debería reproducir, reanudar
            if (currentVolume == 0f && mediaPlayer?.isPlaying == true) {
                pauseMusic()
            } else if (currentVolume > 0f && shouldPlay && !isPausedByUser && mediaPlayer?.isPlaying == false) {
                playMusic()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ajustando volumen: ${e.message}")
        }
    }

    private fun playMusic() {
        try {
            if (mediaPlayer == null) {
                Log.d(TAG, "MediaPlayer nulo, reinicializando...")
                initializeMediaPlayer(currentTrack)
                return
            }

            if (isPrepared && !mediaPlayer!!.isPlaying && shouldPlay && !isPausedByUser) {
                // 🔹 NUEVO: Asegurar que el volumen actual esté aplicado
                mediaPlayer!!.setVolume(currentVolume, currentVolume)
                mediaPlayer!!.start()
                Log.d(TAG, "Música reanudada: $currentTrack con volumen: ${(currentVolume * 100).toInt()}%")
            } else if (!isPrepared) {
                Log.d(TAG, "MediaPlayer no está preparado, esperando...")
            } else if (isPausedByUser) {
                Log.d(TAG, "Música pausada manualmente, no se reanuda automáticamente")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir música: ${e.message}")
            resetMediaPlayer()
        }
    }

    private fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d(TAG, "Música pausada: $currentTrack")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al pausar música: ${e.message}")
        }
    }

    private fun stopMusic() {
        try {
            shouldPlay = false
            isPausedByUser = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPrepared = false
            stopSelf()
            Log.d(TAG, "Servicio de música detenido")
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener música: ${e.message}")
        }
    }

    private fun resetMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPrepared = false
            isPausedByUser = false
            if (shouldPlay) {
                android.os.Handler(mainLooper).postDelayed({
                    initializeMediaPlayer(currentTrack)
                }, 500)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al resetear MediaPlayer: ${e.message}")
        }
    }

    // 🔹 NUEVO: Método para cargar volumen guardado (útil cuando el servicio ya está ejecutándose)
    private fun loadSavedVolume() {
        currentVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
        Log.d(TAG, "Volumen recargado: $currentVolume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy llamado")
        try {
            shouldPlay = false
            isPausedByUser = false
            mediaPlayer?.release()
            mediaPlayer = null
            isPrepared = false
        } catch (e: Exception) {
            Log.e(TAG, "Error en onDestroy: ${e.message}")
        }
    }
}