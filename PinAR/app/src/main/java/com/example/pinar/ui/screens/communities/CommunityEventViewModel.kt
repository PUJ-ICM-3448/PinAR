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

data class CommunityEventUiState(
    val event: CommunityEvent? = null,
    val liveLocations: List<LiveLocation> = emptyList(),
    val isLoading: Boolean = true,
    val isSharingLocation: Boolean = false,
    val selectedParticipant: LiveLocation? = null,
    val routeDestination: LatLng? = null,
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
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { eventRepository.joinEvent(communityId, eventId, uid) }
        }
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

    private fun observeLiveLocations() {
        viewModelScope.launch {
            eventRepository.observeLiveLocations(communityId, eventId).collect { locations ->
                val uid = auth.currentUser?.uid
                _uiState.update {
                    it.copy(
                        liveLocations = locations.filter { loc -> loc.uid != uid }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startSharingLocation() {
        val user = currentUser ?: return
        if (_uiState.value.event?.isActive != true) return
        viewModelScope.launch {
            runCatching {
                eventRepository.startLiveLocation(communityId, eventId, user)
            }.onSuccess {
                _uiState.update { it.copy(isSharingLocation = true) }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    fun stopSharingLocation() {
        val uid = auth.currentUser?.uid ?: return
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _uiState.update { it.copy(isSharingLocation = false) }
        viewModelScope.launch {
            runCatching { eventRepository.stopLiveLocation(communityId, eventId, uid) }
        }
    }

    private fun maybeUpdateLiveLocation(location: LatLng) {
        val user = currentUser ?: return
        val last = lastWrittenLocation
        val shouldWrite = last == null ||
            distanceMeters(last, location) > 15.0
        if (!shouldWrite) return
        lastWrittenLocation = location
        viewModelScope.launch {
            runCatching {
                eventRepository.updateLiveLocation(communityId, eventId, location, user)
            }
        }
    }

    fun selectParticipant(location: LiveLocation) {
        _uiState.update {
            it.copy(
                selectedParticipant = location,
                routeDestination = LatLng(location.latitude, location.longitude)
            )
        }
    }

    fun dismissParticipant() {
        _uiState.update { it.copy(selectedParticipant = null, routeDestination = null) }
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
        if (_uiState.value.isSharingLocation) {
            stopSharingLocation()
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
