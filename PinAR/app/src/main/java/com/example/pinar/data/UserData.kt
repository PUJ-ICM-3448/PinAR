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

fun UserData.sanitized(): UserData = copy(
    uid = (uid as String?) ?: "",
    correo = (correo as String?) ?: "",
    nombre = (nombre as String?) ?: "",
    biografia = (biografia as String?) ?: "",
    fotoUrl = (fotoUrl as String?) ?: "",
    memberOf = memberOf?.map { it.sanitized() } ?: emptyList(),
    FCMToken = (FCMToken as String?) ?: "",
)
