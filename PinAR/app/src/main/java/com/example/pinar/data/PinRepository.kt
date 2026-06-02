package com.example.pinar.data

interface PinRepository {
    suspend fun getVisiblePinsForUser(
        uid: String,
        communities: List<CommunityBasicInfo>
    ): List<PinMapItem>
}
