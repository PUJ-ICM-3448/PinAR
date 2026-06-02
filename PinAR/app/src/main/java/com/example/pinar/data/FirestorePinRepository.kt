package com.example.pinar.data

import com.google.android.gms.maps.model.LatLng

class FirestorePinRepository(
    private val cloudAnchorRepository: CloudAnchorRepository = CloudAnchorRepository()
) : PinRepository {

    override suspend fun getVisiblePinsForUser(
        uid: String,
        communities: List<CommunityBasicInfo>
    ): List<PinMapItem> {
        val communityIds = communities.map { it.id }
        val nameById = communities.associate { it.id to it.name }
        return cloudAnchorRepository.getVisiblePinsForUser(uid, communityIds)
            .map { pin -> toPinMapItem(pin, uid, nameById) }
    }

    private fun toPinMapItem(
        pin: CloudAnchorPin,
        currentUid: String,
        nameById: Map<String, String>
    ): PinMapItem {
        val visibleNames = pin.comunidades.mapNotNull { nameById[it] }.distinct()
        return PinMapItem(
            id = pin.id.ifBlank { pin.cloudAnchorId },
            title = pin.title.ifBlank { "PinAR" },
            subtitle = buildSubtitle(pin),
            position = LatLng(pin.latitude, pin.longitude),
            createdBy = pin.createdBy,
            communityIds = pin.comunidades,
            visibleCommunityNames = visibleNames
        )
    }

    private fun buildSubtitle(pin: CloudAnchorPin): String {
        val buildingLabel = pin.buildingId.takeIf { it.isNotBlank() } ?: "Ubicacion"
        val floorLabel = if (pin.floor > 0) "Piso ${pin.floor}" else ""
        val fallbackLabel = listOf(buildingLabel, floorLabel)
            .filter { it.isNotBlank() }
            .joinToString(" - ")

        return pin.description.ifBlank {
            fallbackLabel.ifBlank { "${pin.latitude}, ${pin.longitude}" }
        }
    }
}
