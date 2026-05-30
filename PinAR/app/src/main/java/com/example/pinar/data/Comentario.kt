package com.example.pinar.data

import com.google.firebase.Timestamp

data class Comentario(
    val id: String = "",
    val pinId: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val fecha: Timestamp? = null
)
