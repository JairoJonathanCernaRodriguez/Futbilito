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

    // Estado para la lista de niveles
    private val _niveles = MutableStateFlow<List<Nivel>>(emptyList())
    val niveles: StateFlow<List<Nivel>> = _niveles

    // Estado para el usuario actual (suponemos un solo usuario)
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    // Cargar todos los niveles
    fun loadNiveles() {
        viewModelScope.launch {
            val nivelesFromDb = db.nivelDao().getAllNiveles()
            _niveles.value = nivelesFromDb
        }
    }

    // Cargar usuario por nombre
    fun loadUsuario() {
        viewModelScope.launch {
            val usuarioFromDb = db.usuarioDao().getUsuario()
            _usuario.value = usuarioFromDb
        }
    }

    // Insertar o actualizar un nivel
    fun saveNivel(nivel: Nivel) {
        viewModelScope.launch {
            db.nivelDao().insertNivel(nivel)
            loadNiveles() // Refrescar lista
        }
    }

    // Insertar o actualizar usuario
    fun saveUsuario(usuario: Usuario) {
        viewModelScope.launch {
            db.usuarioDao().insertUsuario(usuario)
        }
    }

    // Actualizar monedas del usuario (ejemplo de función)
    fun addMonedas(monedasSumar: Int) {
        viewModelScope.launch {
            // Para testing, imprime en logcat
            println("💰 Agregando $monedasSumar monedas")

            // Fuerza una actualización visible
            _usuario.value?.let { currentUser ->
                val updatedUser = currentUser.copy(monedas = currentUser.monedas + monedasSumar)
                _usuario.value = updatedUser
            }

            // Luego actualiza la BD en background
            launch(Dispatchers.IO) {
                val currentUser = db.usuarioDao().getUsuario()
                currentUser?.let {
                    val updatedUser = it.copy(monedas = it.monedas + monedasSumar)
                    db.usuarioDao().updateUsuario(updatedUser)
                }
            }
        }
    }

    fun actualizarPuntuacion(nivelId: Int, puntuacion: Int) {
        viewModelScope.launch {
            db.nivelDao().actualizarPuntuacion(nivelId, puntuacion)
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