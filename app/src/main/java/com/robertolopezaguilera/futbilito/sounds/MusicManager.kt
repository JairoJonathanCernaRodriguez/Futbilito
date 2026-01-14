package com.robertolopezaguilera.futbilito

import android.content.Context
import android.content.Intent
import android.util.Log

object MusicManager {
    private const val TAG = "MusicManager"

    // 🔹 NUEVO: Método para iniciar el servicio y asegurar música MENU
    fun startMenuMusic(context: Context) {
        Log.d(TAG, "🚀 Iniciando servicio de música MENU")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "play")
            putExtra("track", "menu")
        }
        context.startService(intent)
    }

    fun playMenuMusic(context: Context) {
        Log.d(TAG, "🎵 Solicitando música de MENU")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "play")
            putExtra("track", "menu")
        }
        context.startService(intent)
    }

    fun playGameMusic(context: Context) {
        Log.d(TAG, "🎮 Cambiando a música de JUEGO")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "play")
            putExtra("track", "game")
        }
        context.startService(intent)
    }

    fun ensureMenuMusic(context: Context) {
        Log.d(TAG, "🔄 Asegurando música MENU")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "play")
            putExtra("track", "menu")
        }
        context.startService(intent)
    }

    fun resumeMusic(context: Context) {
        Log.d(TAG, "▶️ Reanudando música")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "resume")
        }
        context.startService(intent)
    }

    fun pauseMusic(context: Context) {
        Log.d(TAG, "⏸️ Pausando música")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "pause")
        }
        context.startService(intent)
    }

    fun stopMusic(context: Context) {
        Log.d(TAG, "⏹️ Deteniendo música completamente")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "stop")
        }
        context.startService(intent)
    }

    fun notifyAppInForeground(context: Context) {
        Log.d(TAG, "📱 App en primer plano")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "app_foreground")
        }
        context.startService(intent)
    }

    fun notifyAppInBackground(context: Context) {
        Log.d(TAG, "📱 App en segundo plano")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "app_background")
        }
        context.startService(intent)
    }

    fun setMusicVolume(context: Context, volume: Float) {
        Log.d(TAG, "🔊 Ajustando volumen: $volume")
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("action", "set_volume")
            putExtra("volume", volume)
        }
        context.startService(intent)
    }
}