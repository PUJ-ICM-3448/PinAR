package com.example.pinar.ui.screens.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
        when (state.communityFilter) {
            MapCommunityFilter.OWN_PINS -> list = list.filter { it.createdBy == currentUid }
            MapCommunityFilter.COMMUNITY -> {
                val cid = state.selectedCommunityId
                if (cid != null) list = list.filter { cid in it.communityIds }
            }
            MapCommunityFilter.ALL -> Unit
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

    fun setCommunityFilter(filter: MapCommunityFilter, communityId: String? = null) {
        _uiState.update { applyFilters(it.copy(communityFilter = filter, selectedCommunityId = communityId)) }
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
            try {
                val apiKey = getApiKey()
                if (apiKey.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoadingRoute = false,
                            routeError = "Falta MAPS_API_KEY en la configuracion local"
                        )
                    }
                    return@launch
                }
                val routeUrl = buildString {
                    append("https://maps.googleapis.com/maps/api/directions/json")
                    append("?origin=${origin.latitude},${origin.longitude}")
                    append("&destination=${destination.latitude},${destination.longitude}")
                    append("&key=$apiKey")
                }
                val response = withContext(Dispatchers.IO) {
                    val connection = URL(routeUrl).openConnection() as HttpURLConnection
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.requestMethod = "GET"
                    connection.inputStream.bufferedReader().use { it.readText() }
                        .also { connection.disconnect() }
                }
                val json = JSONObject(response)
                val status = json.optString("status")
                if (status != "OK") {
                    _uiState.update {
                        it.copy(
                            isLoadingRoute = false,
                            routeError = "No hay ruta disponible: $status"
                        )
                    }
                    return@launch
                }
                val encodedPolyline = json
                    .getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")
                val decodedPoints = decodePolyline(encodedPolyline)
                _uiState.update {
                    it.copy(
                        routePolyline = decodedPoints,
                        selectedPin = null,
                        isLoadingRoute = false,
                        isFollowingUser = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoadingRoute = false, routeError = "Error al obtener la ruta")
                }
            }
        }
    }

    fun clearRouteError() {
        _uiState.update { it.copy(routeError = null) }
    }

    private fun getApiKey(): String {
        return try {
            @Suppress("DEPRECATION")
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        var latitude = 0
        var longitude = 0

        while (index < encoded.length) {
            var shift = 0
            var result = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            latitude += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            longitude += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            polyline.add(LatLng(latitude.toDouble() / 1E5, longitude.toDouble() / 1E5))
        }
        return polyline
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        stopStepCounting()
    }
}
