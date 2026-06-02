package com.example.pinar.ui.screens.map

import com.example.pinar.data.PinMapItem
import com.google.android.gms.maps.model.LatLng

enum class MapCommunityFilter {
    ALL,
    OWN_PINS,
    COMMUNITY
}

data class MapUiState(
    val userLocation: LatLng? = null,
    val allPins: List<PinMapItem> = emptyList(),
    val displayPins: List<PinMapItem> = emptyList(),
    val communityFilter: MapCommunityFilter = MapCommunityFilter.ALL,
    val selectedCommunityId: String? = null,
    val searchQuery: String = "",
    val selectedPin: PinMapItem? = null,
    val routeDestination: LatLng? = null,
    val routePolyline: List<LatLng> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val routeError: String? = null,
    val hasLocationPermission: Boolean = false,
    val stepCount: Int = 0,
    val isFollowingUser: Boolean = true,
    val isLoadingPins: Boolean = false,
    val pinsError: String? = null
) {
    val pins: List<PinMapItem> get() = displayPins
}
