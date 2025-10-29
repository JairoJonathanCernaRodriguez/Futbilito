package com.robertolopezaguilera.futbilito.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robertolopezaguilera.futbilito.R

@Entity(tableName = "niveles")
data class Nivel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tiempo: Int,
    val puntuacion: Int,
    val dificultad: String
)

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val monedas: Int
)

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Nivel::class,
            parentColumns = ["id"],
            childColumns = ["nivelId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nivelId: Int,
    val coordenadaX: Int,
    val coordenadaY: Int
){
    constructor(coordenadaX: Int,coordenadaY: Int,nivelId: Int =1):
            this(id = 0,nivelId,coordenadaX,coordenadaY)
}

@Entity(
    tableName = "obstaculos",
    foreignKeys = [
        ForeignKey(
            entity = Nivel::class,
            parentColumns = ["id"],
            childColumns = ["nivelId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Obstaculo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nivelId: Int,
    val coordenadaX: Int,
    val coordenadaY: Int,
    val largo: Int,
    val ancho: Int
) {
    // Constructor secundario para simplificar
    constructor(coordenadaX: Int, coordenadaY: Int, largo: Int, ancho: Int, nivelId: Int = 1) :
            this(0, nivelId, coordenadaX, coordenadaY, largo, ancho)
}

@Entity(
    tableName = "powers",
    foreignKeys = [
        ForeignKey(
            entity = Nivel::class,
            parentColumns = ["id"],
            childColumns = ["nivelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["nivelId"])] // 👈 Agrega índice para mejor rendimiento
)
data class Powers(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nivelId: Int,

    val coordenadaX: Int,

    val coordenadaY: Int,

    val tipo: String // 👈 Tipo del poder: "speed_boost", "ghost_mode", etc.
) {
    // Constructor secundario opcional (puedes eliminarlo si no lo usas)
    constructor(coordenadaX: Int, coordenadaY: Int, nivelId: Int = 1, tipo: String) :
            this(id = 0, nivelId, coordenadaX, coordenadaY, tipo)
}

@Entity(tableName = "tienda_items")
data class TiendaItem(
    @PrimaryKey val id: Int,
    val nombre: String,
    val tipo: TipoItem, // FONDO, PELOTA, OBSTACULO, ICONO
    val precio: Int,
    val colorHex: String? = null, // Para colores simples
    val imagenResId: Int? = null, // Para imágenes
    val desbloqueado: Boolean = false,
    val seleccionado: Boolean = false
)

enum class TipoItem {
    FONDO, PELOTA, OBSTACULO, ICONO
}

// data/UsuarioPersonalizacion.kt
@Entity(tableName = "usuario_personalizacion")
data class UsuarioPersonalizacion(
    @PrimaryKey val id: Int = 1, // Siempre 1 para el usuario actual
    val fondoSeleccionado: Int = 1,
    val pelotaSeleccionada: Int = 1,
    val obstaculoSeleccionado: Int = 1,
    val iconoSeleccionado: Int = 1
)

data class GamePersonalizacion(
    val colorFondo: Color = Color(0xFF2E3440),
    val colorPelota: Color = Color(0xFFBF616A),
    val colorObstaculos: Color = Color(0xFF5E81AC),
    val iconoPowerUp: Int = R.drawable.ic_power,
    val iconoFantasma: Int = R.drawable.ic_ghost,
    val iconoPelota: Int = R.drawable.ic_ballsoccer // 👈 NUEVO: Icono para la pelota
)
