package com.example.pinar.ui.screens.communities

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CommunityEvent
import com.example.pinar.data.CommunityEventRepository
import com.example.pinar.data.LiveLocation
import com.example.pinar.data.UserData
import com.example.pinar.ui.screens.map.util.DirectionsHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

data class CommunityEventUiState(
    val event: CommunityEvent? = null,
    val liveLocations: List<LiveLocation> = emptyList(),
    val isLoading: Boolean = true,
    val isSharingLocation: Boolean = false,
    val selectedParticipant: LiveLocation? = null,
    val routeDestination: LatLng? = null,
    val routePolyline: List<LatLng> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val routeError: String? = null,
    val userLocation: LatLng? = null,
    val error: String? = null
)

class CommunityEventViewModel @JvmOverloads constructor(
    application: Application,
    private val eventRepository: CommunityEventRepository = CommunityEventRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AndroidViewModel(application) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application.applicationContext)

    private val _uiState = MutableStateFlow(CommunityEventUiState())
    val uiState: StateFlow<CommunityEventUiState> = _uiState.asStateFlow()

    private var communityId: String = ""
    private var eventId: String = ""
    private var currentUser: UserData? = null
    private var lastWrittenLocation: LatLng? = null
    private var liveLocationsJob: Job? = null
    private var locationUpdatesActive = false

    private val locationRequest = LocationRequest.Builder(4000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateIntervalMillis(1500L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                val latLng = LatLng(loc.latitude, loc.longitude)
                _uiState.update { it.copy(userLocation = latLng) }
                if (_uiState.value.isSharingLocation) {
                    maybeUpdateLiveLocation(latLng)
                }
            }
        }
    }

    fun init(communityId: String, eventId: String, user: UserData?) {
        this.communityId = communityId
        this.eventId = eventId
        this.currentUser = user
        loadEvent()
        observeLiveLocations()
        restoreSharingState()
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { eventRepository.joinEvent(communityId, eventId, uid) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val event = eventRepository.getEvent(communityId, eventId)
            val expired = event?.expiresAt?.toDate()?.time?.let { it < System.currentTimeMillis() } == true
            val active = event?.isActive == true && !expired
            _uiState.update {
                it.copy(
                    event = event?.copy(isActive = active),
                    isLoading = false,
                    error = if (event == null) "Evento no encontrado" else null
                )
            }
        }
    }

    private fun restoreSharingState() {
        val user = currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val existing = runCatching {
                eventRepository.getMyLiveLocation(communityId, eventId, uid)
            }.getOrNull() ?: return@launch
            lastWrittenLocation = LatLng(existing.latitude, existing.longitude)
            _uiState.update {
                it.copy(
                    isSharingLocation = true,
                    userLocation = LatLng(existing.latitude, existing.longitude)
                )
            }
            startLocationUpdatesIfNeeded()
        }
    }

    private fun observeLiveLocations() {
        liveLocationsJob?.cancel()
        liveLocationsJob = viewModelScope.launch {
            eventRepository.observeLiveLocations(communityId, eventId).collect { update ->
                val uid = auth.currentUser?.uid
                _uiState.update {
                    it.copy(
                        liveLocations = update.locations.filter { loc -> loc.uid != uid },
                        error = update.errorMessage ?: it.error
                    )
                }
            }
        }
    }

    fun onShareLocationClicked(hasLocationPermission: Boolean) {
        if (!hasLocationPermission) return
        startSharingLocation()
    }

    @SuppressLint("MissingPermission")
    fun ensureLocationUpdatesIfSharing() {
        if (_uiState.value.isSharingLocation) {
            startLocationUpdatesIfNeeded()
        }
    }

    @SuppressLint("MissingPermission")
    fun startSharingLocation() {
        val user = currentUser ?: run {
            _uiState.update { it.copy(error = "Debes iniciar sesión para compartir ubicación") }
            return
        }
        if (_uiState.value.event?.isActive != true) return
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val latLng = fetchCurrentLocation()
            if (latLng == null) {
                _uiState.update {
                    it.copy(error = "No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo.")
                }
                return@launch
            }
            runCatching {
                eventRepository.startLiveLocation(communityId, eventId, user, latLng)
            }.onSuccess {
                lastWrittenLocation = latLng
                _uiState.update {
                    it.copy(isSharingLocation = true, userLocation = latLng, error = null)
                }
                startLocationUpdatesIfNeeded()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "No se pudo compartir la ubicación")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdatesIfNeeded() {
        if (locationUpdatesActive) return
        locationUpdatesActive = true
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopSharingLocation() {
        val uid = auth.currentUser?.uid ?: return
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationUpdatesActive = false
        lastWrittenLocation = null
        _uiState.update { it.copy(isSharingLocation = false) }
        viewModelScope.launch {
            runCatching { eventRepository.stopLiveLocation(communityId, eventId, uid) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "No se pudo detener el compartido de ubicación")
                    }
                }
        }
    }

    private fun maybeUpdateLiveLocation(location: LatLng) {
        val user = currentUser ?: return
        val last = lastWrittenLocation
        val shouldWrite = last == null || distanceMeters(last, location) > 15.0
        if (!shouldWrite) return
        lastWrittenLocation = location
        viewModelScope.launch {
            runCatching {
                eventRepository.updateLiveLocation(communityId, eventId, location, user)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "Error al actualizar ubicación")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation(): LatLng? {
        val last = runCatching { fusedLocationClient.lastLocation.await() }.getOrNull()
        if (last != null) {
            return LatLng(last.latitude, last.longitude)
        }
        return suspendCancellableCoroutine { cont ->
            val tokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { tokenSource.cancel() }
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { loc ->
                    cont.resume(loc?.let { LatLng(it.latitude, it.longitude) })
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    fun selectParticipant(location: LiveLocation) {
        _uiState.update {
            it.copy(
                selectedParticipant = location,
                routeDestination = LatLng(location.latitude, location.longitude),
                routePolyline = emptyList(),
                routeError = null
            )
        }
    }

    fun dismissParticipant() {
        _uiState.update {
            it.copy(
                selectedParticipant = null,
                routeDestination = null,
                routePolyline = emptyList(),
                routeError = null
            )
        }
    }

    fun fetchRouteToParticipant() {
        val destination = _uiState.value.routeDestination ?: return
        val origin = _uiState.value.userLocation ?: run {
            _uiState.update {
                it.copy(routeError = "Tu ubicacion actual no esta disponible aun")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoute = true, routeError = null) }
            DirectionsHelper.fetchRoute(getApplication(), origin, destination)
                .onSuccess { points ->
                    _uiState.update {
                        it.copy(routePolyline = points, isLoadingRoute = false)
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

    private fun distanceMeters(a: LatLng, b: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude, a.longitude, b.latitude, b.longitude, results
        )
        return results[0].toDouble()
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationUpdatesActive = false
    }
}
