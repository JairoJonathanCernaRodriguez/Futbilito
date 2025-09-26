package com.robertolopezaguilera.futbilito.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.ui.CategoriaConProgreso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(private val db: GameDatabase) : ViewModel() {

    private val _niveles = MutableStateFlow<List<Nivel>>(emptyList())
    val niveles: StateFlow<List<Nivel>> = _niveles

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    // 👇 Nuevo estado para monedas (backup)
    private val _monedas = MutableStateFlow(0)
    val monedas: StateFlow<Int> = _monedas

    init {
        // Cargar datos automáticamente al crear el ViewModel
        loadUsuario()
        loadNiveles()
    }

    // 👇 MEJORADA: Cargar usuario con manejo de errores
    fun loadUsuario() {
        viewModelScope.launch {
            try {
                val usuarioFromDb = db.usuarioDao().getUsuario()
                _usuario.value = usuarioFromDb
                _monedas.value = usuarioFromDb?.monedas ?: 0
                println("👤 Usuario cargado: ${usuarioFromDb?.nombre}, Monedas: ${usuarioFromDb?.monedas}")
            } catch (e: Exception) {
                println("❌ Error cargando usuario: ${e.message}")
            }
        }
    }
    fun loadNiveles() {
        viewModelScope.launch {
            val nivelesFromDb = db.nivelDao().getAllNiveles()
            _niveles.value = nivelesFromDb
        }
    }

    fun saveNivel(nivel: Nivel) {
        viewModelScope.launch {
            db.nivelDao().insertNivel(nivel)
            loadNiveles()
        }
    }

    fun saveUsuario(usuario: Usuario) {
        viewModelScope.launch {
            db.usuarioDao().insertUsuario(usuario)
            _usuario.value = usuario
            _monedas.value = usuario.monedas
        }
    }

    // 👇 MEJORADA: Función para agregar monedas
    fun addMonedas(monedasSumar: Int) {
        viewModelScope.launch {
            println("💰 Intentando agregar $monedasSumar monedas")

            try {
                // Obtener usuario actual de la base de datos (más confiable)
                val currentUser = db.usuarioDao().getUsuario()
                currentUser?.let { usuario ->
                    val nuevasMonedas = usuario.monedas + monedasSumar
                    val updatedUser = usuario.copy(monedas = nuevasMonedas)

                    // Actualizar ambos: StateFlow y base de datos
                    _usuario.value = updatedUser
                    _monedas.value = nuevasMonedas
                    db.usuarioDao().updateUsuario(updatedUser)

                    println("✅ Monedas actualizadas: $nuevasMonedas")
                } ?: run {
                    println("❌ No se encontró usuario para agregar monedas")
                }
            } catch (e: Exception) {
                println("❌ Error agregando monedas: ${e.message}")
            }
        }
    }

    fun actualizarPuntuacion(nivelId: Int, puntuacion: Int) {
        viewModelScope.launch {
            db.nivelDao().actualizarPuntuacion(nivelId, puntuacion)
        }
    }

    // 👇 MEJORADA: Función para obtener monedas actuales
    fun getCurrentCoins(): Int {
        val coinsFromState = _usuario.value?.monedas ?: _monedas.value
        println("💳 Consultando monedas: $coinsFromState")
        return coinsFromState
    }

    // 👇 MEJORADA: Función para obtener monedas de forma asíncrona (más confiable)
    suspend fun getCurrentCoinsFromDb(): Int {
        return try {
            val usuario = db.usuarioDao().getUsuario()
            val coins = usuario?.monedas ?: 0
            println("💳 Monedas desde BD: $coins")
            coins
        } catch (e: Exception) {
            println("❌ Error obteniendo monedas de BD: ${e.message}")
            0
        }
    }

    // 👇 MEJORADA: Función para restar monedas
    fun restarMonedas(monedasRestar: Int) {
        viewModelScope.launch {
            try {
                val currentUser = db.usuarioDao().getUsuario()
                currentUser?.let { usuario ->
                    if (usuario.monedas >= monedasRestar) {
                        val nuevasMonedas = usuario.monedas - monedasRestar
                        val updatedUser = usuario.copy(monedas = nuevasMonedas)

                        _usuario.value = updatedUser
                        _monedas.value = nuevasMonedas
                        db.usuarioDao().updateUsuario(updatedUser)

                        println("✅ Monedas restadas. Nuevo total: $nuevasMonedas")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error restando monedas: ${e.message}")
            }
        }
    }

    // 👇 NUEVA: Función para verificar si se pueden restar monedas
    suspend fun puedeRestarMonedas(monedasRestar: Int): Boolean {
        return try {
            val usuario = db.usuarioDao().getUsuario()
            val puede = usuario?.monedas ?: 0 >= monedasRestar
            println("💳 Verificación de monedas: $puede (Actual: ${usuario?.monedas}, Requerido: $monedasRestar)")
            puede
        } catch (e: Exception) {
            println("❌ Error verificando monedas: ${e.message}")
            false
        }
    }
}

class GameViewModelFactory(private val db: GameDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NivelViewModel(private val dao: NivelDao) : ViewModel() {

    val categoriasConProgreso: StateFlow<List<CategoriaConProgreso>> =
        dao.getAllNivelesFlow()
            .map { niveles ->
                // Orden de las categorías (de más fácil a más difícil)
                val ordenCategorias = listOf("Tutorial", "Principiante", "Medio", "Avanzado", "Experto")

                // Procesar cada categoría
                val categoriasProcesadas = mutableListOf<CategoriaConProgreso>()

                for (categoria in ordenCategorias) {
                    val nivelesDeCategoria = niveles.filter { it.dificultad == categoria }

                    if (nivelesDeCategoria.isNotEmpty()) {
                        val totalNiveles = nivelesDeCategoria.size
                        val puntosTotales = totalNiveles * 4
                        val puntosObtenidos = nivelesDeCategoria.sumOf { it.puntuacion.coerceIn(0, 4) }

                        // Determinar si está desbloqueada
                        val isUnlocked = when (categoria) {
                            "Tutorial" -> true // Tutorial siempre desbloqueado
                            else -> {
                                // Buscar la categoría anterior en la lista ya procesada
                                val indexActual = ordenCategorias.indexOf(categoria)
                                val categoriaAnterior = ordenCategorias.getOrNull(indexActual - 1)

                                categoriaAnterior?.let { anterior ->
                                    categoriasProcesadas.find { it.dificultad == anterior }?.let { catAnterior ->
                                        // Se desbloquea si la categoría anterior tiene al menos la mitad de estrellas
                                        catAnterior.puntosObtenidos >= catAnterior.puntosTotales / 2
                                    } ?: false
                                } ?: false
                            }
                        }

                        categoriasProcesadas.add(
                            CategoriaConProgreso(
                                dificultad = categoria,
                                totalNiveles = totalNiveles,
                                puntosObtenidos = puntosObtenidos,
                                puntosTotales = puntosTotales,
                                isUnlocked = isUnlocked
                            )
                        )
                    }
                }

                categoriasProcesadas
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getNivelesPorCategoria(categoria: String): Flow<List<Nivel>> {
        return dao.getNivelesPorCategoria(categoria)
    }
}