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
