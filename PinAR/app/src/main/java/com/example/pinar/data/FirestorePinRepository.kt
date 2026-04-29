package com.example.pinar.data

import com.google.android.gms.maps.model.LatLng

class FirestorePinRepository(
    private val cloudAnchorRepository: CloudAnchorRepository = CloudAnchorRepository()
) : PinRepository {

    override suspend fun getPins(): List<PinMapItem> {
        return cloudAnchorRepository.getAllPins()
            .filter { it.latitude != 0.0 || it.longitude != 0.0 }
            .map { pin ->
                PinMapItem(
                    id = pin.id.ifBlank { pin.cloudAnchorId },
                    title = pin.title.ifBlank { "Pin sin titulo" },
                    subtitle = buildSubtitle(pin),
                    position = LatLng(pin.latitude, pin.longitude)
                )
            }
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
