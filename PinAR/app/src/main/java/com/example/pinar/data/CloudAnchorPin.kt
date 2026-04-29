package com.example.pinar.data

data class CloudAnchorPin(
    val id: String = "",
    val cloudAnchorId: String = "",
    val title: String = "",
    val description: String = "",
    val buildingId: String = "",
    val floor: Int = 0,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val ttlDays: Int = 365
)
