package com.robertolopezaguilera.futbilito.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
