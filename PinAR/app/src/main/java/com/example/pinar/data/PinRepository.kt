package com.example.pinar.data

interface PinRepository {
    suspend fun getPins(): List<PinMapItem>
}
