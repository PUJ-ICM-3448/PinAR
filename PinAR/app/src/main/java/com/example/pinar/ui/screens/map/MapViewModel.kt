package com.example.pinar.ui.screens.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.FirestorePinRepository
import com.example.pinar.data.PinMapItem
import com.example.pinar.data.PinRepository
import com.example.pinar.ui.screens.map.util.DirectionsHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel @JvmOverloads constructor(
    application: Application,
    private val pinRepository: PinRepository = FirestorePinRepository()
) : AndroidViewModel(application), SensorEventListener {

    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val cloudAnchorRepository = CloudAnchorRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var initialSteps = -1
    private var currentUid: String = ""
    private var memberCommunities: List<CommunityBasicInfo> = emptyList()

    private val locationRequest = LocationRequest.Builder(4000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateIntervalMillis(1500L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                _uiState.update {
                    it.copy(userLocation = LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    fun setUserContext(uid: String, communities: List<CommunityBasicInfo>) {
        if (uid == currentUid && communities == memberCommunities) return
        currentUid = uid
        memberCommunities = communities
        loadPins()
    }

    private fun applyFilters(state: MapUiState): MapUiState {
        var list = state.allPins
        
        // Si no hay filtros específicos, mostrar todo (MapUiState.isFilterAllSelected)
        if (!state.isFilterAllSelected) {
            list = list.filter { pin ->
                val matchesOwn = if (state.filterOwnPins) pin.createdBy == currentUid else false
                val matchesCommunity = pin.communityIds.any { it in state.selectedCommunityIds }
                
                matchesOwn || matchesCommunity
            }
        }

        if (state.searchQuery.isNotBlank()) {
            list = list.filter { it.title.contains(state.searchQuery, ignoreCase = true) }
        }
        return state.copy(displayPins = list)
    }

    fun loadPins() {
        if (currentUid.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPins = true, pinsError = null) }
            runCatching {
                pinRepository.getVisiblePinsForUser(currentUid, memberCommunities)
            }
                .onSuccess { pins ->
                    _uiState.update { applyFilters(it.copy(allPins = pins, isLoadingPins = false)) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoadingPins = false,
                            pinsError = "No fue posible cargar los pines"
                        )
                    }
                }
        }
    }

    fun toggleFilterAll() {
        _uiState.update { applyFilters(it.copy(filterOwnPins = false, selectedCommunityIds = emptySet())) }
    }

    fun toggleFilterOwnPins() {
        _uiState.update { applyFilters(it.copy(filterOwnPins = !it.filterOwnPins)) }
    }

    fun toggleCommunityFilter(communityId: String) {
        _uiState.update { state ->
            val newIds = if (communityId in state.selectedCommunityIds) {
                state.selectedCommunityIds - communityId
            } else {
                state.selectedCommunityIds + communityId
            }
            applyFilters(state.copy(selectedCommunityIds = newIds))
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { applyFilters(it.copy(searchQuery = query)) }
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    fun onActivityPermissionChanged(granted: Boolean) {
        if (granted) {
            startStepCounting()
        } else {
            stopStepCounting()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!_uiState.value.hasLocationPermission) return
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startStepCounting() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialSteps == -1) {
                initialSteps = totalSteps
            }
            val currentSessionSteps = totalSteps - initialSteps
            _uiState.update { it.copy(stepCount = currentSessionSteps) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setFollowingUser(enabled: Boolean) {
        _uiState.update { it.copy(isFollowingUser = enabled) }
    }

    fun selectPin(pin: PinMapItem) {
        _uiState.update {
            it.copy(
                selectedPin = pin,
                routeDestination = pin.position,
                routeError = null,
                isFollowingUser = false
            )
        }
        cloudAnchorRepository.actualizarVisita(pin.id)
    }

    fun dismissSelectedPin() {
        _uiState.update { it.copy(selectedPin = null) }
    }

    fun clearRoute() {
        _uiState.update {
            it.copy(routePolyline = emptyList(), selectedPin = null, routeDestination = null)
        }
    }

    fun fetchRouteToSelectedPin() {
        val destination = _uiState.value.routeDestination ?: return
        fetchRoute(destination)
    }

    private fun fetchRoute(destination: LatLng) {
        val origin = _uiState.value.userLocation ?: run {
            _uiState.update {
                it.copy(routeError = "Tu ubicacion actual no esta disponible aun")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoute = true, routeError = null) }
            DirectionsHelper.fetchRoute(context, origin, destination)
                .onSuccess { decodedPoints ->
                    _uiState.update {
                        it.copy(
                            routePolyline = decodedPoints,
                            selectedPin = null,
                            isLoadingRoute = false,
                            isFollowingUser = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingRoute = false,
                            routeError = e.message ?: "Error al obtener la ruta"
                        )
                    }
                }
        }
    }

    fun clearRouteError() {
        _uiState.update { it.copy(routeError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        stopStepCounting()
    }
}
