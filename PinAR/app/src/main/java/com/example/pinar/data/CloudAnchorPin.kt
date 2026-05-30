package com.example.pinar.data

import com.google.firebase.Timestamp

data class CloudAnchorPin(
    val id: String = "",
    val cloudAnchorId: String = "",
    val title: String = "",
    val description: String = "",
    val buildingId: String = "",
    val floor: Int = 0,
    val createdBy: String = "",
    val fecha: Timestamp? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val ttlDays: Int = 365,
    val likes: Int = 0,
    val visitas: Int = 0
)
