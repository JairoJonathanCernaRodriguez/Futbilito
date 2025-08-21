package com.robertolopezaguilera.futbilito.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Nivel::class, Usuario::class, Item::class, Obstaculo::class],
    version = 2, // ⚠️ incrementa versión porque añadimos tablas nuevas
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun nivelDao(): NivelDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun itemDao(): ItemDao
    abstract fun obstaculoDao(): ObstaculoDao

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
                    .fallbackToDestructiveMigration() // ⚠️ borra datos si cambias DB
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
