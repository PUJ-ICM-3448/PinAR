package com.example.pinar.ui.screens.map

import com.example.pinar.data.PinMapItem
import com.example.pinar.data.UserData
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val userLocation: LatLng? = null,
    val pins: List<PinMapItem> = emptyList(),
    val selectedPin: PinMapItem? = null,
    val routeDestination: LatLng? = null,
    val routePolyline: List<LatLng> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val routeError: String? = null,
    val hasLocationPermission: Boolean = false,
    val unreadCount: Int = 3,
    val stepCount: Int = 0,
    val isFollowingUser: Boolean = true,
    // Usuarios que comparten su ubicación (excluye al usuario actual)
    val otherUsers: List<UserData> = emptyList(),
    val selectedUser: UserData? = null
)
