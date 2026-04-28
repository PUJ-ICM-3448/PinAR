package com.example.pinar.data

import com.google.firebase.Timestamp

data class UserData(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = "",
    val biografia: String = "",
    val fotoUrl: String = "",
    val creacion: Timestamp? = null,
    val esAdmin: Boolean = false
)
