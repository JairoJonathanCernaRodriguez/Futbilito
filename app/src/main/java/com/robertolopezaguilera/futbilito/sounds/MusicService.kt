package com.robertolopezaguilera.futbilito

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack = MusicTrack.MENU
    private var currentVolume: Float = 0.7f
    private var isAppInForeground = true
    private var isInitialized = false

    private lateinit var prefs: android.content.SharedPreferences

    // 🔹 NUEVO: Crear nuestro propio CoroutineScope para el servicio
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var retryJob: Job? = null

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
        Log.d(TAG, "🎵 Servicio de música creado - INICIANDO MÚSICA MENU")
        prefs = getSharedPreferences("audio_settings", android.content.Context.MODE_PRIVATE)
        currentVolume = prefs.getFloat(PREF_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)

        // 🔹 INICIAR MÚSICA INMEDIATAMENTE AL CREAR EL SERVICIO
        initializeMediaPlayer(MusicTrack.MENU)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🎵 Comando recibido: ${intent?.getStringExtra("action")}")

        when (intent?.getStringExtra("action")) {
            "play" -> handlePlay(intent)
            "pause" -> pauseMusic()
            "stop" -> stopMusic()
            "resume" -> resumeMusic()
            "set_volume" -> {
                val volume = intent.getFloatExtra("volume", 0.7f)
                setVolume(volume)
                prefs.edit().putFloat(PREF_MUSIC_VOLUME, volume).apply()
            }
            "app_foreground" -> {
                isAppInForeground = true
                resumeMusic()
            }
            "app_background" -> {
                isAppInForeground = false
                pauseMusic()
            }
        }

        // 🔹 IMPORTANTE: START_STICKY hace que el servicio se reinicie si es eliminado
        return START_STICKY
    }

    private fun handlePlay(intent: Intent) {
        val requestedTrack = when (intent.getStringExtra("track")) {
            "game" -> MusicTrack.GAME
            else -> MusicTrack.MENU
        }

        Log.d(TAG, "🔄 Solicitando track: $requestedTrack (actual: $currentTrack)")

        // 🔹 SOLO CAMBIAR SI ES DIFERENTE EL TRACK
        if (currentTrack != requestedTrack) {
            changeTrack(requestedTrack)
        } else {
            // Si es el mismo track, solo asegurar que esté reproduciéndose
            Log.d(TAG, "✅ Mismo track - asegurando reproducción")
            if (isInitialized && mediaPlayer?.isPlaying == false && isAppInForeground) {
                playMusic()
            }
        }
    }

    private fun initializeMediaPlayer(track: MusicTrack) {
        try {
            // Limpiar anterior si existe
            mediaPlayer?.release()
            mediaPlayer = null

            // 🔹 CANCELAR cualquier job de reintento anterior
            retryJob?.cancel()

            mediaPlayer = MediaPlayer().apply {
                val afd = resources.openRawResourceFd(track.resourceId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                isLooping = true // 🔹 LOOP INFINITO
                setVolume(currentVolume, currentVolume)

                setOnPreparedListener { mp ->
                    Log.d(TAG, "✅ MediaPlayer preparado para: $track")
                    currentTrack = track
                    isInitialized = true
                    if (isAppInForeground) {
                        mp.start()
                        Log.d(TAG, "▶️ Música iniciada: $track")
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "❌ Error en MediaPlayer: $what, $extra")
                    isInitialized = false
                    // 🔹 CORREGIDO: Usar serviceScope en lugar de lifecycleScope
                    retryJob = serviceScope.launch {
                        delay(1000)
                        Log.d(TAG, "🔄 Reintentando inicializar MediaPlayer después de error")
                        initializeMediaPlayer(currentTrack)
                    }
                    true
                }

                setOnCompletionListener {
                    // Esto no debería pasar porque tenemos loop, pero por si acaso
                    Log.w(TAG, "⚠️ MediaPlayer completado - reiniciando por loop")
                    if (isAppInForeground) {
                        mediaPlayer?.start()
                    }
                }

                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar MediaPlayer: ${e.message}")
            // 🔹 CORREGIDO: Reintentar también en caso de excepción
            retryJob = serviceScope.launch {
                delay(1000)
                Log.d(TAG, "🔄 Reintentando inicializar MediaPlayer después de excepción")
                initializeMediaPlayer(track)
            }
        }
    }

    private fun changeTrack(newTrack: MusicTrack) {
        Log.d(TAG, "🔄 Cambiando track de $currentTrack a $newTrack")
        initializeMediaPlayer(newTrack)
    }

    private fun resumeMusic() {
        if (isAppInForeground && isInitialized && mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start()
                Log.d(TAG, "▶️ Música reanudada: $currentTrack")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al reanudar música: ${e.message}")
            }
        }
    }

    private fun playMusic() {
        if (!isAppInForeground) {
            Log.d(TAG, "📱 App en background, no se reproduce música")
            return
        }

        try {
            if (isInitialized && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d(TAG, "▶️ Música iniciada: $currentTrack")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al reproducir música: ${e.message}")
        }
    }

    private fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d(TAG, "⏸️ Música pausada")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al pausar música: ${e.message}")
        }
    }

    private fun stopMusic() {
        try {
            // 🔹 CANCELAR cualquier job pendiente
            retryJob?.cancel()

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
            Log.d(TAG, "⏹️ Música detenida y recursos liberados")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al detener música: ${e.message}")
        }
    }

    private fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
        Log.d(TAG, "🔊 Volumen ajustado: ${(currentVolume * 100).toInt()}%")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🚨 Servicio de música destruido")

        // 🔹 LIMPIAR recursos
        retryJob?.cancel()
        serviceScope.cancel() // Cancelar el scope completo
        stopMusic()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 App removida del recents - deteniendo servicio")
        stopSelf()
    }
}