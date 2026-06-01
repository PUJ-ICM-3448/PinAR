package com.example.pinar.data

import com.google.firebase.Timestamp

data class UserData(
    val uid: String = "",
    val correo: String = "",
    var nombre: String = "",
    var biografia: String = "",
    var fotoUrl: String = "",
    val creacion: Timestamp? = null,
    val esAdmin: Boolean = false,
    val memberOf: List<CommunityBasicInfo> = emptyList(),
    var compartirUbicacion: Boolean = false,
    var latitud: Double? = null,
    var longitud: Double? = null,
    val FCMToken: String = "",
)
