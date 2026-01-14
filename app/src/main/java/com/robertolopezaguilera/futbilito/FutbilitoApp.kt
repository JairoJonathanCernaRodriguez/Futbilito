package com.robertolopezaguilera.futbilito

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log

class FutbilitoApp : Application() {

    companion object {
        private const val TAG = "FutbilitoApp"

        // 🔹 Singleton para acceder desde cualquier lugar
        private var instance: FutbilitoApp? = null

        fun getInstance(): FutbilitoApp {
            return instance ?: throw IllegalStateException("FutbilitoApp no está inicializado")
        }
    }

    // 🔹 Estados globales de la aplicación
    private var _currentScreen: String = "menu"
    private val screenListeners = mutableListOf<(String) -> Unit>()

    // 🔹 Control de ciclo de vida
    private var appInForeground = true
    private var activityCount = 0

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "🎮 FutbilitoApp inicializada")

        // 🔹 Registrar lifecycle callback para toda la app
        registerActivityLifecycleCallbacks(appLifecycleCallbacks)
        registerComponentCallbacks(appComponentCallbacks)
    }

    // 🔹 Métodos para control de pantalla
    fun setCurrentScreen(screen: String) {
        if (_currentScreen != screen) {
            Log.d(TAG, "🔄 Cambiando pantalla: $_currentScreen -> $screen")
            _currentScreen = screen

            // Notificar a todos los listeners
            screenListeners.forEach { listener ->
                try {
                    listener(screen)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en screen listener: ${e.message}")
                }
            }

            // 🔹 Control automático de música basado en pantalla
            handleMusicForScreen(screen)
        }
    }

    fun getCurrentScreen(): String = _currentScreen

    fun addScreenListener(listener: (String) -> Unit) {
        screenListeners.add(listener)
    }

    fun removeScreenListener(listener: (String) -> Unit) {
        screenListeners.remove(listener)
    }

    // 🔹 Control centralizado de música
    private fun handleMusicForScreen(screen: String) {
        if (!appInForeground) {
            Log.d(TAG, "App en background, no manejando música")
            return
        }

//        when (screen) {
//            "game" -> {
//                Log.d(TAG, "🎵 Solicitando música de JUEGO")
//                MusicManager.playGameMusicSafely(this)
//            }
//            else -> {
//                Log.d(TAG, "🎵 Solicitando música de MENÚ")
//                MusicManager.playMenuMusicSafely(this)
//            }
//        }
    }

    // 🔹 Callbacks de ciclo de vida de actividades
    private val appLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "Activity creada: ${activity::class.java.simpleName}")
        }

        override fun onActivityStarted(activity: Activity) {
            activityCount++
            Log.d(TAG, "Activity iniciada: ${activity::class.java.simpleName}, Count: $activityCount")

            if (activityCount == 1 && !appInForeground) {
                // App volviendo a primer plano
                appInForeground = true
                Log.d(TAG, "🔄 App volvió a primer plano")
                MusicManager.notifyAppInForeground(this@FutbilitoApp)

                // 🔹 Reanudar música para la pantalla actual
                handleMusicForScreen(_currentScreen)
            }
        }

        override fun onActivityResumed(activity: Activity) {
            Log.d(TAG, "Activity resumida: ${activity::class.java.simpleName}")

            // 🔹 Determinar tipo de pantalla basado en la actividad
            val screenType = when (activity) {
                is GameActivity -> "game"
                else -> "menu"
            }
            setCurrentScreen(screenType)
        }

        override fun onActivityPaused(activity: Activity) {
            Log.d(TAG, "Activity pausada: ${activity::class.java.simpleName}")
        }

        override fun onActivityStopped(activity: Activity) {
            activityCount--
            Log.d(TAG, "Activity detenida: ${activity::class.java.simpleName}, Count: $activityCount")

            if (activityCount == 0) {
                // App yendo a segundo plano
                appInForeground = false
                Log.d(TAG, "🔄 App yendo a segundo plano")
                MusicManager.notifyAppInBackground(this@FutbilitoApp)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "Activity destruida: ${activity::class.java.simpleName}")
        }
    }

    // 🔹 Callbacks para configuración del sistema
    private val appComponentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {}

        override fun onLowMemory() {
            Log.w(TAG, "⚠️ Memoria baja, limpiando recursos")
        }

        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                    Log.d(TAG, "🔄 UI oculta, pausando música")
                    MusicManager.pauseMusic(this@FutbilitoApp)
                }
                ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
                ComponentCallbacks2.TRIM_MEMORY_MODERATE,
                ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                    Log.w(TAG, "⚠️ Memoria crítica, limpiando recursos")
                    // Podemos liberar recursos adicionales aquí si es necesario
                }
            }
        }
    }

    // 🔹 Limpieza
    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "🚨 FutbilitoApp terminando")
        unregisterActivityLifecycleCallbacks(appLifecycleCallbacks)
        unregisterComponentCallbacks(appComponentCallbacks)

        // Detener música completamente
        MusicManager.stopMusic(this)
        instance = null
    }
}