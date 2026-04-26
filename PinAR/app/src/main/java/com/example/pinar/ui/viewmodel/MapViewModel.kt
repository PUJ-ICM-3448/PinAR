package com.example.pinar.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.ExistingBackendPinRepository
import com.example.pinar.data.PinMapItem
import com.example.pinar.data.PinRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
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

data class MapUiState(
    val userLocation: LatLng? = null,
    val pins: List<PinMapItem> = emptyList(),
    val selectedPin: PinMapItem? = null,
    val routeDestination: LatLng? = null,
    val routePolyline: List<LatLng> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val routeError: String? = null,
    val hasLocationPermission: Boolean = false
)

class MapViewModel(
    application: Application,
    private val pinRepository: PinRepository = ExistingBackendPinRepository()
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

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

    init {
        loadPins()
    }

    fun loadPins() {
        viewModelScope.launch {
            runCatching { pinRepository.getPins() }
                .onSuccess { pins ->
                    _uiState.update { it.copy(pins = pins) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(routeError = "No fue posible cargar los pines")
                    }
                }
        }
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
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

    fun selectPin(pin: PinMapItem) {
        _uiState.update {
            it.copy(
                selectedPin = pin,
                routeDestination = pin.position,
                routeError = null
            )
        }
    }

    fun dismissSelectedPin() {
        _uiState.update { it.copy(selectedPin = null) }
    }

    fun clearRoute() {
        _uiState.update { it.copy(routePolyline = emptyList(), selectedPin = null, routeDestination = null) }
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
                        isLoadingRoute = false
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
    }
}
