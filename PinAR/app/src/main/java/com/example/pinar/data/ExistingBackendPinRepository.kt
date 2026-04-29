package com.example.pinar.data

import com.google.android.gms.maps.model.LatLng

//placeholder
class ExistingBackendPinRepository : PinRepository {
    override suspend fun getPins(): List<PinMapItem> {
        return listOf(
            PinMapItem(
                id = "conf-a",
                title = "Sala de Conferencias A",
                subtitle = "Edificio Principal - Piso 3",
                position = LatLng(4.62815, -74.06476)
            ),
            PinMapItem(
                id = "cafeteria",
                title = "Cafeteria",
                subtitle = "Edificio Principal - Piso 1",
                position = LatLng(4.62756, -74.06412)
            ),
            PinMapItem(
                id = "lab-204",
                title = "Laboratorio 204",
                subtitle = "Edificio de investigacion - Piso 2",
                position = LatLng(4.62861, -74.06344)
            )
        )
    }
}
