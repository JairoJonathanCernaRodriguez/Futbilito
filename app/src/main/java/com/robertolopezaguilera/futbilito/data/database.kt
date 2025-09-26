package com.robertolopezaguilera.futbilito.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Nivel::class, Usuario::class, Item::class, Obstaculo::class, Powers::class], // 👈 Agrega Powers
    version = 3, // ⚠️ Incrementa versión porque añadimos nueva tabla
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun nivelDao(): NivelDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun itemDao(): ItemDao
    abstract fun obstaculoDao(): ObstaculoDao
    abstract fun powersDao(): PowersDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "laberinto_db"
                )
                    .fallbackToDestructiveMigration() // ⚠️ Esto borra datos al cambiar versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
