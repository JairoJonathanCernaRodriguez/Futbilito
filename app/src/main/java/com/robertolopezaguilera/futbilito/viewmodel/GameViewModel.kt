package com.robertolopezaguilera.futbilito.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.ui.CategoriaConProgreso
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
            val currentUser = db.usuarioDao().getUsuario()
            if (currentUser != null) {
                val updatedUser = currentUser.copy(monedas = currentUser.monedas + monedasSumar)
                db.usuarioDao().updateUsuario(updatedUser)
                _usuario.value = updatedUser
            }
        }
    }

    fun actualizarPuntuacion(nivelId: Int, puntuacion: Int) {
        viewModelScope.launch {
            db.nivelDao().actualizarPuntuacion(nivelId, puntuacion)
        }
    }
}


class NivelViewModel(private val dao: NivelDao) : ViewModel() {

    val categoriasConProgreso: StateFlow<List<CategoriaConProgreso>> =
        dao.getAllNivelesFlow() // Esto devuelve Flow<List<Nivel>>
            .map { niveles ->
                niveles
                    .groupBy { it.dificultad }
                    .map { (dificultad, nivelesDeCategoria) ->
                        val totalNiveles = nivelesDeCategoria.size
                        val puntosTotales = totalNiveles * 4
                        val puntosObtenidos = nivelesDeCategoria.sumOf { it.puntuacion }
                        CategoriaConProgreso(
                            dificultad = dificultad,
                            totalNiveles = totalNiveles,
                            puntosObtenidos = puntosObtenidos,
                            puntosTotales = puntosTotales
                        )
                    }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getNivelesPorCategoria(categoria: String): Flow<List<Nivel>> {
        return dao.getNivelesPorCategoria(categoria)
    }
}

