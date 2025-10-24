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
    private var currentTrack: MusicTrack = MusicTrack.MENU // 🔹 NUEVO: Track actual

    // 🔹 NUEVO: Enum para los diferentes tracks
    enum class MusicTrack(val resourceId: Int) {
        MENU(R.raw.game_music_loop_14),    // Música de menús
        GAME(R.raw.game_music_loop_19)     // Música de juego
    }

    companion object {
        private const val TAG = "MusicService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio de música creado")
        // 🔹 CAMBIO: No inicializar automáticamente, esperar comando
    }

    private fun initializeMediaPlayer(track: MusicTrack = MusicTrack.MENU) {
        try {
            // Liberar cualquier instancia previa
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer()
            currentTrack = track // 🔹 Actualizar track actual

            mediaPlayer?.apply {
                // Configurar el data source con el track específico
                val afd = resources.openRawResourceFd(track.resourceId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                // Configurar listeners
                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer preparado correctamente para: $track")
                    isPrepared = true
                    if (shouldPlay && !isPausedByUser) {
                        mp.start()
                        Log.d(TAG, "Música reproduciéndose: $track")
                    }
                }

                setOnCompletionListener { mp ->
                    Log.d(TAG, "Canción completada: $track, reiniciando inmediatamente...")
                    if (shouldPlay && isPrepared && !isPausedByUser) {
                        // Reiniciar inmediatamente sin delay
                        mp.seekTo(0)
                        mp.start()
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Error en MediaPlayer para $track - what: $what, extra: $extra")
                    // Reintentar después de un breve delay
                    android.os.Handler(mainLooper).postDelayed({
                        resetMediaPlayer()
                    }, 1000)
                    true
                }

                // Preparar de forma asíncrona
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

        // 🔹 NUEVO: Obtener el track solicitado (por defecto MENU)
        val requestedTrack = when (intent?.getStringExtra("track")) {
            "game" -> MusicTrack.GAME
            "menu" -> MusicTrack.MENU
            else -> MusicTrack.MENU // Por defecto
        }

        when (intent?.getStringExtra("action")) {
            "play" -> {
                // 🔹 NUEVO: Cambiar track si es diferente al actual
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
                // 🔹 NUEVO: Marcar como pausado por usuario
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
            }
            else -> {
                // Por defecto: play con track solicitado
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
            mediaPlayer?.setVolume(volume, volume)
            Log.d(TAG, "Volumen de música ajustado a: ${(volume * 100).toInt()}%")
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
                mediaPlayer!!.start()
                Log.d(TAG, "Música reanudada: $currentTrack")
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
                // Reintentar después de un breve delay
                android.os.Handler(mainLooper).postDelayed({
                    initializeMediaPlayer(currentTrack)
                }, 500)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al resetear MediaPlayer: ${e.message}")
        }
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