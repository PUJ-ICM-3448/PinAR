package com.example.pinar.data

/**
 * Modelo de datos para un Pin con Cloud Anchor.
 * Se persiste en Firestore (colección "cloud_anchor_pins").
 *
 * Ref: https://developers.google.com/ar/develop/java/cloud-anchors/developer-guide
 */
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
