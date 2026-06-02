package com.example.pinar.ui.screens.ar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.R
import com.example.pinar.ar.CloudAnchorManager
import com.example.pinar.data.ARSessionState
import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.CommunityRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ARViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CloudAnchorRepository = CloudAnchorRepository(),
    private val communityRepository: CommunityRepository = CommunityRepository()
) : AndroidViewModel(application) {
    private val _state = mutableStateOf(ARState())
    val state: State<ARState> = _state

    private val cloudAnchorManager = CloudAnchorManager()
    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var localAnchor: Anchor? = null

    companion object {
        private const val TAG = "ARViewModel"
    }

    fun onSessionStateChange(newState: ARSessionState) {
        _state.value = _state.value.copy(sessionState = newState)
    }

    fun onSessionCreated(session: Session) {
        cloudAnchorManager.enableCloudAnchors(session)
        _state.value = _state.value.copy(
            sessionState = _state.value.sessionState.copy(session = session)
        )
        Log.d(TAG, "Sesion creada y Cloud Anchors habilitados")
    }

    fun startHostingMode() {
        _state.value = _state.value.copy(
            isHostingMode = true,
            hostingState = HostingState.PLACING,
            errorMessage = null
        )
    }

    fun cancelHosting() {
        localAnchor?.detach()
        localAnchor = null
        _state.value = _state.value.copy(
            isHostingMode = false,
            hostingState = HostingState.IDLE,
            featureMapQuality = null,
            showPinDialog = false,
            selectedCommunityIds = emptySet(),
            errorMessage = null
        )
    }

    fun onPlaneTapped(hitResult: HitResult) {
        if (_state.value.hostingState != HostingState.PLACING) return

        val trackable = hitResult.trackable
        if (trackable !is Plane || !trackable.isPoseInPolygon(hitResult.hitPose)) return

        localAnchor?.detach()
        localAnchor = hitResult.createAnchor()
        _state.value = _state.value.copy(hostingState = HostingState.MAPPING)
        Log.d(TAG, "Anchor local creado. Mapeando entorno.")
    }

    fun updateFeatureMapQuality(cameraPose: Pose) {
        val session = _state.value.sessionState.session ?: return
        if (_state.value.hostingState != HostingState.MAPPING) return

        try {
            val quality = cloudAnchorManager.estimateHostingQuality(session, cameraPose)
            _state.value = _state.value.copy(featureMapQuality = quality)
        } catch (e: Exception) {
            Log.w(TAG, "Error evaluando calidad: ${e.message}")
        }
    }

    fun showPinDetailsDialog() {
        _state.value = _state.value.copy(showPinDialog = true)
    }

    fun onPinTitleChange(title: String) {
        _state.value = _state.value.copy(pendingPinTitle = title)
    }

    fun onPinDescriptionChange(description: String) {
        _state.value = _state.value.copy(pendingPinDescription = description)
    }

    fun toggleCommunitySelection(communityId: String) {
        val current = _state.value.selectedCommunityIds
        _state.value = _state.value.copy(
            selectedCommunityIds = if (communityId in current) {
                current - communityId
            } else {
                current + communityId
            }
        )
    }

    fun confirmAndHostPin() {
        val session = _state.value.sessionState.session ?: return
        val anchor = localAnchor ?: return
        val title = _state.value.pendingPinTitle

        if (title.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "El titulo es obligatorio")
            return
        }

        viewModelScope.launch {
            val location = getCurrentPinLocation()
            if (location == null) {
                _state.value = _state.value.copy(
                    hostingState = HostingState.ERROR,
                    showPinDialog = false,
                    errorMessage = getLocationErrorMessage()
                )
                return@launch
            }

            _state.value = _state.value.copy(
                hostingState = HostingState.UPLOADING,
                showPinDialog = false,
                errorMessage = null
            )
            Log.d(
                TAG,
                "Hosting iniciado para pin: $title en ${location.latitude}, ${location.longitude}"
            )

            cloudAnchorManager.hostCloudAnchor(session, anchor) { cloudAnchorId, state ->
                if (state == Anchor.CloudAnchorState.SUCCESS && cloudAnchorId != null) {
                    Log.d(TAG, "Hosting exitoso. Cloud Anchor ID: $cloudAnchorId")
                    savePinToFirestore(cloudAnchorId, location)
                } else {
                    Log.e(TAG, "Error hosting: $state")
                    _state.value = _state.value.copy(
                        hostingState = HostingState.ERROR,
                        errorMessage = "Error hosting: $state"
                    )
                }
            }
        }
    }

    private fun savePinToFirestore(cloudAnchorId: String, location: Location) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val pin = CloudAnchorPin(
            cloudAnchorId = cloudAnchorId,
            title = _state.value.pendingPinTitle,
            description = _state.value.pendingPinDescription,
            buildingId = "default",
            floor = 1,
            createdBy = currentUser?.uid ?: "anonymous",
            fecha = Timestamp.now(),
            latitude = location.latitude,
            longitude = location.longitude,
            ttlDays = CloudAnchorManager.DEFAULT_TTL_DAYS,
            likes = 0,
            visitas = 0
        )

        val selectedCommunities = _state.value.selectedCommunityIds

        viewModelScope.launch {
            try {
                val pinId = repository.savePin(pin)
                sharePinWithCommunities(pinId, selectedCommunities)
                _state.value = _state.value.copy(
                    hostingState = HostingState.SUCCESS,
                    isHostingMode = false,
                    pendingPinTitle = "",
                    pendingPinDescription = "",
                    selectedCommunityIds = emptySet(),
                    featureMapQuality = null
                )
                localAnchor = null
                Log.d(TAG, "Pin guardado en Firestore: ${pin.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando pin en Firestore", e)
                _state.value = _state.value.copy(
                    hostingState = HostingState.ERROR,
                    errorMessage = "Error guardando: ${e.message}"
                )
            }
        }
    }

    private suspend fun sharePinWithCommunities(pinId: String, communityIds: Set<String>) {
        if (communityIds.isEmpty()) return
        val failures = mutableListOf<String>()
        for (communityId in communityIds) {
            runCatching { communityRepository.sharePinWithCommunity(communityId, pinId) }
                .onFailure { e ->
                    Log.w(TAG, "No se pudo compartir pin en comunidad $communityId", e)
                    failures.add(communityId)
                }
        }
        if (failures.isNotEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = "Pin publicado, pero no se pudo compartir en ${failures.size} comunidad(es)"
            )
        }
    }

    fun resolveNearbyPins() {
        val session = _state.value.sessionState.session ?: return

        viewModelScope.launch {
            try {
                val pins = repository.getAllPins()
                _state.value = _state.value.copy(
                    availablePins = pins,
                    resolvedPins = emptyList(),
                    errorMessage = null
                )
                Log.d(TAG, "Cargados ${pins.size} pines de Firestore")

                for (pin in pins) {
                    if (pin.cloudAnchorId.isNotEmpty()) {
                        cloudAnchorManager.resolveCloudAnchor(
                            session,
                            pin.cloudAnchorId
                        ) { anchor, state ->
                            if (state == Anchor.CloudAnchorState.SUCCESS && anchor != null) {
                                if (_state.value.resolvedPins.none { it.pinData.cloudAnchorId == pin.cloudAnchorId }) {
                                    val resolvedPin = ResolvedPin(anchor = anchor, pinData = pin)
                                    _state.value = _state.value.copy(
                                        resolvedPins = _state.value.resolvedPins + resolvedPin
                                    )
                                    Log.d(TAG, "Pin resuelto: ${pin.title}")
                                }
                            } else {
                                Log.e(TAG, "Error resolviendo ${pin.cloudAnchorId}: $state")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando pines", e)
                _state.value = _state.value.copy(
                    errorMessage = "Error cargando pines: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun resetHostingState() {
        _state.value = _state.value.copy(hostingState = HostingState.IDLE)
    }


    private fun getLocationErrorMessage(): String {
        return if (hasLocationPermission()) {
            context.getString(R.string.no_fue_posible_obtener_su_ubicacion_actual_intenta_de_nuevo)
        } else {
            context.getString(R.string.se_necesita_permiso_de_ubicacion_para_guardar_el_pin_con_coordenadas)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentPinLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        val cancellationTokenSource = CancellationTokenSource()
        val currentLocation = awaitTaskResultOrNull(
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
        ) {
            cancellationTokenSource.cancel()
        }

        return currentLocation ?: awaitTaskResultOrNull(fusedLocationClient.lastLocation)
    }

    private suspend fun <T> awaitTaskResultOrNull(
        task: Task<T>,
        onCancellation: (() -> Unit)? = null
    ): T? = suspendCancellableCoroutine { continuation ->
        task.addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        task.addOnFailureListener { error ->
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
        task.addOnCanceledListener {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
        continuation.invokeOnCancellation {
            onCancellation?.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        localAnchor?.detach()
    }
}
