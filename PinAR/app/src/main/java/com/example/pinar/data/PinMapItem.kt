package com.example.pinar.data

import com.google.android.gms.maps.model.LatLng

data class PinMapItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val position: LatLng
)
