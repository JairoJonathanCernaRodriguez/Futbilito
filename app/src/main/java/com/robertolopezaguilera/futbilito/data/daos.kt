package com.robertolopezaguilera.futbilito.data

import androidx.room.*
import com.robertolopezaguilera.futbilito.ui.CategoriaConProgreso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface NivelDao {
    @Query("SELECT * FROM niveles ORDER BY id ASC")
    suspend fun getAllNiveles(): List<Nivel>

    @Query("SELECT * FROM niveles")
    fun getAllNivelesFlow(): Flow<List<Nivel>>

    @Query("SELECT * FROM niveles WHERE id = :nivel")
    suspend fun getNivel(nivel: Int): Nivel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNivel(nivel: Nivel)
    @Insert
    fun insertNiveles(niveles: List<Nivel>)

    @Update
    suspend fun updateNivel(nivel: Nivel)

    @Query("UPDATE niveles SET puntuacion = :puntuacion WHERE id = :nivelId")
    suspend fun actualizarPuntuacion(nivelId: Int, puntuacion: Int)

    @Delete
    suspend fun deleteNivel(nivel: Nivel)

    @Query("SELECT * FROM niveles WHERE dificultad = :categoria ORDER BY id ASC")
    fun getNivelesPorCategoria(categoria: String): Flow<List<Nivel>>


}

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("UPDATE usuarios SET monedas = :monedas WHERE id = 1")
    suspend fun actualizarMonedas(monedas: Int)

    @Delete
    suspend fun deleteUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUsuario(): Usuario?

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuarioFlow(): Flow<Usuario?>

}

@Dao
interface ItemDao {

    // Insertar un item (si ya existe reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    //Insertar lista
    @Insert
    suspend fun insertListItem(intems: List<Item>)

    // Borrar todos los items de un nivel
    @Query("DELETE FROM items WHERE nivelId = :nivelId")
    suspend fun deleteItemsByNivel(nivelId: Int)

    // Obtener todos los items de un nivel
    @Query("SELECT * FROM items WHERE nivelId = :nivelId")
    fun getItemsByNivel(nivelId: Int): Flow<List<Item>>
}

@Dao
interface PowersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPower(powers: Powers) // 👈 Corregí el nombre (isertar → insertar)

    @Query("SELECT * FROM powers WHERE nivelId = :nivelId")
    fun getPowersByNivel(nivelId: Int): Flow<List<Powers>> // 👈 Quita suspend aquí
}

@Dao
interface ObstaculoDao {

    // Insertar un obstáculo (si ya existe reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObstaculo(obstaculo: Obstaculo)

    //Insertar lista
    @Insert
    suspend fun insertListObstculo(osbtaculos: List<Obstaculo>)

    // Borrar todos los obstáculos de un nivel
    @Query("DELETE FROM obstaculos WHERE nivelId = :nivelId")
    suspend fun deleteObstaculosByNivel(nivelId: Int)

    // Obtener todos los obstáculos de un nivel
    @Query("SELECT * FROM obstaculos WHERE nivelId = :nivelId")
    fun getObstaculosByNivel(nivelId: Int): Flow<List<Obstaculo>>
}

// data/TiendaDao.kt
@Dao
interface TiendaDao {
    @Query("SELECT * FROM tienda_items WHERE tipo = :tipo")
    fun getItemsPorTipo(tipo: TipoItem): Flow<List<TiendaItem>>

    @Query("SELECT * FROM tienda_items WHERE desbloqueado = 1")
    fun getItemsDesbloqueados(): Flow<List<TiendaItem>>

    @Query("SELECT * FROM tienda_items WHERE id = :id")
    suspend fun getItemPorId(id: Int): TiendaItem?

    @Update
    suspend fun actualizarItem(item: TiendaItem)

    @Query("SELECT * FROM usuario_personalizacion WHERE id = 1")
    suspend fun getPersonalizacionUsuario(): UsuarioPersonalizacion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPersonalizacion(personalizacion: UsuarioPersonalizacion)

    @Query("SELECT * FROM tienda_items")
    fun getAllItems(): Flow<List<TiendaItem>>

    @Query("UPDATE tienda_items SET desbloqueado = :desbloqueado WHERE id = :id")
    suspend fun updateDesbloqueado(id: Int, desbloqueado: Boolean)

    @Query("UPDATE tienda_items SET seleccionado = :seleccionado WHERE id = :id")
    suspend fun updateSeleccionado(id: Int, seleccionado: Boolean)

    @Query("UPDATE tienda_items SET seleccionado = 0 WHERE tipo = :tipo")
    suspend fun deseleccionarTodosPorTipo(tipo: TipoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TiendaItem>)

    @Query("DELETE FROM tienda_items")
    suspend fun deleteAll()
}