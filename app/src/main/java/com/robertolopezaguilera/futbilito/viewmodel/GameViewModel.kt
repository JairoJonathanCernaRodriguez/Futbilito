package com.robertolopezaguilera.futbilito.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Nivel
import com.robertolopezaguilera.futbilito.data.NivelDao
import com.robertolopezaguilera.futbilito.data.TiendaItem
import com.robertolopezaguilera.futbilito.data.TipoItem
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.ui.CategoriaConProgreso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.TiendaDao
import kotlinx.coroutines.flow.first

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
    fun actualizarNombreUsuario(nuevoNombre: String) {
        viewModelScope.launch {
            try {
                println("👤 Intentando actualizar nombre a: $nuevoNombre")

                // Obtener usuario actual
                val currentUser = db.usuarioDao().getUsuario()
                currentUser?.let { usuario ->
                    // Crear usuario actualizado
                    val updatedUser = usuario.copy(nombre = nuevoNombre.trim())

                    // Actualizar ambos: StateFlow y base de datos
                    _usuario.value = updatedUser
                    db.usuarioDao().updateUsuario(updatedUser)

                    println("✅ Nombre actualizado correctamente: $nuevoNombre")

                    // 🔹 OPCIONAL: Verificar que se guardó correctamente
                    val usuarioVerificado = db.usuarioDao().getUsuario()
                    println("🔍 Verificación - Nombre en BD: ${usuarioVerificado?.nombre}")

                } ?: run {
                    println("❌ No se encontró usuario para actualizar nombre")

                    // 🔹 OPCIONAL: Crear usuario si no existe
                    val nuevoUsuario = Usuario(id = 1, nombre = nuevoNombre.trim(), monedas = 0)
                    _usuario.value = nuevoUsuario
                    db.usuarioDao().insertUsuario(nuevoUsuario)
                    println("✅ Usuario creado con nombre: $nuevoNombre")
                }
            } catch (e: Exception) {
                println("❌ Error actualizando nombre: ${e.message}")
                e.printStackTrace()
            }
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

class TiendaViewModel(
    private val gameViewModel: GameViewModel,
    private val tiendaDao: TiendaDao
) : ViewModel() {

    private val _itemsFondo = MutableStateFlow<List<TiendaItem>>(emptyList())
    val itemsFondo: StateFlow<List<TiendaItem>> = _itemsFondo

    private val _itemsPelota = MutableStateFlow<List<TiendaItem>>(emptyList())
    val itemsPelota: StateFlow<List<TiendaItem>> = _itemsPelota

    private val _itemsObstaculo = MutableStateFlow<List<TiendaItem>>(emptyList())
    val itemsObstaculo: StateFlow<List<TiendaItem>> = _itemsObstaculo

    private val _itemsIcono = MutableStateFlow<List<TiendaItem>>(emptyList())
    val itemsIcono: StateFlow<List<TiendaItem>> = _itemsIcono

    init {
        cargarItemsDesdeBD()
    }

    private fun cargarItemsDesdeBD() {
        viewModelScope.launch {
            try {
                val itemsFromDb = tiendaDao.getAllItems().first()
                if (itemsFromDb.isEmpty()) {
                    // Si no hay datos, insertar los iniciales
                    insertarDatosIniciales()
                } else {
                    // Cargar datos desde BD
                    actualizarListsDesdeBD(itemsFromDb)
                }
            } catch (e: Exception) {
                println("❌ Error cargando items desde BD: ${e.message}")
                // En caso de error, cargar datos en memoria como respaldo
                cargarDatosEnMemoriaComoRespaldo()
            }
        }
    }

    private suspend fun insertarDatosIniciales() {
        val itemsIniciales = crearListaItemsIniciales()
        tiendaDao.insertAll(itemsIniciales)
        actualizarListsDesdeBD(itemsIniciales)
    }

    // 👇 NUEVO: Método único para crear la lista de items
    private fun crearListaItemsIniciales(): List<TiendaItem> {
        return listOf(
            // 👇 FONDOS
            TiendaItem(1, "Azul Oscuro", TipoItem.FONDO, 0, "#0D1B4A", desbloqueado = true, seleccionado = true),
            TiendaItem(2, "Verde Bosque", TipoItem.FONDO, 100, "#1B5E20", desbloqueado = false),
            TiendaItem(3, "Rojo Pasión", TipoItem.FONDO, 150, "#B71C1C", desbloqueado = false),
            TiendaItem(4, "Púrpura Místico", TipoItem.FONDO, 200, "#4A148C", desbloqueado = false),
            TiendaItem(5, "Noche Estrellada", TipoItem.FONDO, 300, "#0A2463", desbloqueado = false),
            TiendaItem(27, "Amarillo Sol", TipoItem.FONDO, 120, "#F57F17", desbloqueado = false),
            TiendaItem(28, "Naranja Cálido", TipoItem.FONDO, 180, "#E65100", desbloqueado = false),
            TiendaItem(29, "Rosa Vibrante", TipoItem.FONDO, 220, "#C2185B", desbloqueado = false),
            TiendaItem(30, "Cian Profundo", TipoItem.FONDO, 160, "#006064", desbloqueado = false),
            TiendaItem(31, "Gris Oscuro", TipoItem.FONDO, 90, "#212121", desbloqueado = false),
            TiendaItem(32, "Verde Azulado", TipoItem.FONDO, 140, "#004D40", desbloqueado = false),
            TiendaItem(33, "Azul Cielo", TipoItem.FONDO, 110, "#0277BD", desbloqueado = false),
            TiendaItem(34, "Morado Real", TipoItem.FONDO, 190, "#6A1B9A", desbloqueado = false),
            TiendaItem(35, "Café Oscuro", TipoItem.FONDO, 130, "#3E2723", desbloqueado = false),

            // 👇 PELOTAS
            TiendaItem(6, "Rojo Clásico", TipoItem.PELOTA, 0, "#BF616A", desbloqueado = true, seleccionado = true),
            TiendaItem(7, "Azul Eléctrico", TipoItem.PELOTA, 80, "#2196F3", desbloqueado = false),
            TiendaItem(8, "Verde Esmeralda", TipoItem.PELOTA, 120, "#4CAF50", desbloqueado = false),
            TiendaItem(9, "Dorado Brillante", TipoItem.PELOTA, 200, "#FFD700", desbloqueado = false),
            TiendaItem(10, "Naranja Fuego", TipoItem.PELOTA, 150, "#FF5722", desbloqueado = false),
            TiendaItem(36, "Rosa Neón", TipoItem.PELOTA, 100, "#E91E63", desbloqueado = false),
            TiendaItem(37, "Púrpura Mágico", TipoItem.PELOTA, 130, "#9C27B0", desbloqueado = false),
            TiendaItem(38, "Cian Brillante", TipoItem.PELOTA, 110, "#00BCD4", desbloqueado = false),
            TiendaItem(39, "Lima Vibrante", TipoItem.PELOTA, 90, "#CDDC39", desbloqueado = false),
            TiendaItem(40, "Coral Cálido", TipoItem.PELOTA, 120, "#FF7043", desbloqueado = false),
            TiendaItem(41, "Azul Marino", TipoItem.PELOTA, 140, "#303F9F", desbloqueado = false),
            TiendaItem(42, "Verde Lima", TipoItem.PELOTA, 95, "#AFB42B", desbloqueado = false),
            TiendaItem(43, "Magenta", TipoItem.PELOTA, 160, "#C2185B", desbloqueado = false),
            TiendaItem(44, "Turquesa", TipoItem.PELOTA, 125, "#009688", desbloqueado = false),
            TiendaItem(45, "Violeta", TipoItem.PELOTA, 145, "#7B1FA2", desbloqueado = false),

            // 👇 OBSTÁCULOS
            TiendaItem(11, "Azul Standard", TipoItem.OBSTACULO, 0, "#5E81AC", desbloqueado = true, seleccionado = true),
            TiendaItem(12, "Gris Metal", TipoItem.OBSTACULO, 90, "#607D8B", desbloqueado = false),
            TiendaItem(13, "Verde Agua", TipoItem.OBSTACULO, 130, "#009688", desbloqueado = false),
            TiendaItem(14, "Naranja", TipoItem.OBSTACULO, 180, "#FF9800", desbloqueado = false),
            TiendaItem(15, "Rosa", TipoItem.OBSTACULO, 220, "#E91E63", desbloqueado = false),
            TiendaItem(46, "Rojo Oscuro", TipoItem.OBSTACULO, 150, "#C62828", desbloqueado = false),
            TiendaItem(47, "Verde Oscuro", TipoItem.OBSTACULO, 140, "#2E7D32", desbloqueado = false),
            TiendaItem(48, "Púrpura Oscuro", TipoItem.OBSTACULO, 170, "#6A1B9A", desbloqueado = false),
            TiendaItem(49, "Amarillo Mostaza", TipoItem.OBSTACULO, 120, "#F9A825", desbloqueado = false),
            TiendaItem(50, "Cian Oscuro", TipoItem.OBSTACULO, 160, "#00838F", desbloqueado = false),
            TiendaItem(51, "Marrón", TipoItem.OBSTACULO, 110, "#5D4037", desbloqueado = false),
            TiendaItem(52, "Azul Grisáceo", TipoItem.OBSTACULO, 100, "#546E7A", desbloqueado = false),
            TiendaItem(53, "Verde Oliva", TipoItem.OBSTACULO, 130, "#827717", desbloqueado = false),
            TiendaItem(54, "Rojo Ladrillo", TipoItem.OBSTACULO, 145, "#D84315", desbloqueado = false),
            TiendaItem(55, "Azul Acero", TipoItem.OBSTACULO, 125, "#455A64", desbloqueado = false),

            // 👇 ICONOS
            TiendaItem(16, "Simple", TipoItem.ICONO, 0, null, R.drawable.ic_ballsimple, desbloqueado = true, seleccionado = true),
            TiendaItem(17, "Basketball", TipoItem.ICONO, 200, null, R.drawable.ic_ballbasketball, desbloqueado = false),
            TiendaItem(18, "Corazón", TipoItem.ICONO, 180, null, R.drawable.ic_ballheart, desbloqueado = false),
            TiendaItem(19, "Rayo", TipoItem.ICONO, 130, null, R.drawable.ic_balllightning, desbloqueado = false),
            TiendaItem(20, "Planeta", TipoItem.ICONO, 140, null, R.drawable.ic_ballplanet, desbloqueado = false),
            TiendaItem(21, "Pool", TipoItem.ICONO, 200, null, R.drawable.ic_ballpool, desbloqueado = false),
            TiendaItem(22, "Boliche", TipoItem.ICONO, 125, null, R.drawable.ic_ballsharp, desbloqueado = false),
            TiendaItem(23, "Balón Fútbol", TipoItem.ICONO, 200, null, R.drawable.ic_ballsoccer, desbloqueado = false),
            TiendaItem(24, "Tennis", TipoItem.ICONO, 110, null, R.drawable.ic_balltennis, desbloqueado = false),
            TiendaItem(25, "Volleyball", TipoItem.ICONO, 120, null, R.drawable.ic_ballvolleyball, desbloqueado = false),
            TiendaItem(26, "Estrella", TipoItem.ICONO, 150, null, R.drawable.ic_star, desbloqueado = false),
            TiendaItem(56, "Beisbol", TipoItem.ICONO, 115, null, R.drawable.ic_ballbaseball, desbloqueado = false),
            TiendaItem(57, "Rugby", TipoItem.ICONO, 125, null, R.drawable.ic_ballrugby, desbloqueado = false),
            TiendaItem(60, "Ping Pong", TipoItem.ICONO, 105, null, R.drawable.ic_ballpingpong, desbloqueado = false)
        )
    }

    // 👇 ACTUALIZADO: Solo para respaldo en caso de error
    private fun cargarDatosEnMemoriaComoRespaldo() {
        viewModelScope.launch {
            val itemsRespaldo = crearListaItemsIniciales()
            actualizarListsDesdeBD(itemsRespaldo)
            println("⚠️ Cargando datos en memoria como respaldo")
        }
    }

    private fun actualizarListsDesdeBD(items: List<TiendaItem>) {
        _itemsFondo.value = items.filter { it.tipo == TipoItem.FONDO }
        _itemsPelota.value = items.filter { it.tipo == TipoItem.PELOTA }
        _itemsObstaculo.value = items.filter { it.tipo == TipoItem.OBSTACULO }
        _itemsIcono.value = items.filter { it.tipo == TipoItem.ICONO }

        println("✅ Datos cargados: ${_itemsFondo.value.size} fondos, ${_itemsPelota.value.size} pelotas, ${_itemsObstaculo.value.size} obstáculos, ${_itemsIcono.value.size} iconos")
    }

    // 👇 ELIMINADO: cargarDatosIniciales() ya no es necesario
    // 👇 ELIMINADO: cargarDatosEnMemoria() reemplazado por cargarDatosEnMemoriaComoRespaldo()

    // ... el resto de tus métodos permanecen igual (comprarItem, seleccionarItem, etc.)
    fun comprarItem(item: TiendaItem) {
        viewModelScope.launch {
            val monedasActuales = gameViewModel.usuario.value?.monedas ?: 0
            if (monedasActuales >= item.precio && !item.desbloqueado) {
                try {
                    // Descontar monedas
                    gameViewModel.restarMonedas(item.precio)

                    // Actualizar en BD
                    tiendaDao.updateDesbloqueado(item.id, true)

                    // Actualizar en memoria
                    actualizarItemEnMemoria(item.copy(desbloqueado = true))

                    println("✅ Item ${item.nombre} comprado y guardado en BD")
                } catch (e: Exception) {
                    println("❌ Error comprando item: ${e.message}")
                    // Revertir monedas en caso de error
                    gameViewModel.addMonedas(item.precio)
                }
            }
        }
    }

    fun seleccionarItem(item: TiendaItem) {
        viewModelScope.launch {
            if (item.desbloqueado) {
                try {
                    // Deseleccionar todos los items del mismo tipo en BD
                    tiendaDao.deseleccionarTodosPorTipo(item.tipo)

                    // Seleccionar el item actual en BD
                    tiendaDao.updateSeleccionado(item.id, true)

                    // Actualizar en memoria
                    when (item.tipo) {
                        TipoItem.FONDO -> {
                            _itemsFondo.value = _itemsFondo.value.map {
                                it.copy(seleccionado = it.id == item.id)
                            }
                        }
                        TipoItem.PELOTA -> {
                            _itemsPelota.value = _itemsPelota.value.map {
                                it.copy(seleccionado = it.id == item.id)
                            }
                        }
                        TipoItem.OBSTACULO -> {
                            _itemsObstaculo.value = _itemsObstaculo.value.map {
                                it.copy(seleccionado = it.id == item.id)
                            }
                        }
                        TipoItem.ICONO -> {
                            _itemsIcono.value = _itemsIcono.value.map {
                                it.copy(seleccionado = it.id == item.id)
                            }
                        }
                    }

                    println("✅ Item ${item.nombre} seleccionado y guardado en BD")
                } catch (e: Exception) {
                    println("❌ Error seleccionando item: ${e.message}")
                }
            }
        }
    }

    private fun actualizarItemEnMemoria(itemActualizado: TiendaItem) {
        when (itemActualizado.tipo) {
            TipoItem.FONDO -> {
                _itemsFondo.value = _itemsFondo.value.map {
                    if (it.id == itemActualizado.id) itemActualizado else it
                }
            }
            TipoItem.PELOTA -> {
                _itemsPelota.value = _itemsPelota.value.map {
                    if (it.id == itemActualizado.id) itemActualizado else it
                }
            }
            TipoItem.OBSTACULO -> {
                _itemsObstaculo.value = _itemsObstaculo.value.map {
                    if (it.id == itemActualizado.id) itemActualizado else it
                }
            }
            TipoItem.ICONO -> {
                _itemsIcono.value = _itemsIcono.value.map {
                    if (it.id == itemActualizado.id) itemActualizado else it
                }
            }
        }
    }

    fun getColorFondoSeleccionado(): Color {
        return try {
            val hex = _itemsFondo.value.find { it.seleccionado }?.colorHex ?: "#0D1B4A"
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF0D1B4A)
        }
    }

    fun getColorPelotaSeleccionado(): Color {
        return try {
            val hex = _itemsPelota.value.find { it.seleccionado }?.colorHex ?: "#BF616A"
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFFBF616A)
        }
    }

    fun getColorObstaculoSeleccionado(): Color {
        return try {
            val hex = _itemsObstaculo.value.find { it.seleccionado }?.colorHex ?: "#5E81AC"
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF5E81AC)
        }
    }

    fun getIconoSeleccionado(): Int {
        return _itemsIcono.value.find { it.seleccionado }?.imagenResId ?: R.drawable.ic_ballsimple
    }

    fun getIconoPelotaSeleccionado(): Int {
        return _itemsIcono.value.find { it.seleccionado }?.imagenResId ?: R.drawable.ic_ballsimple
    }
}

class TiendaViewModelFactory(
    private val gameViewModel: GameViewModel,
    private val tiendaDao: TiendaDao // 👈 NUEVO: Recibir el DAO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TiendaViewModel(gameViewModel, tiendaDao) as T // 👈 Pasar el DAO
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}