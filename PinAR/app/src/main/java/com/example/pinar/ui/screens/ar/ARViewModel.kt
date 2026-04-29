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
import com.example.pinar.ar.CloudAnchorManager
import com.example.pinar.data.ARSessionState
import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.CloudAnchorRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ARViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = mutableStateOf(ARState())
    val state: State<ARState> = _state

    private val cloudAnchorManager = CloudAnchorManager()
    private val repository = CloudAnchorRepository()
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
        Log.d(TAG, "Anchor local creado. Mapeando entorno...")
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

    fun confirmAndHostPin() {
        val session = _state.value.sessionState.session ?: return
        val anchor = localAnchor ?: return
        val title = _state.value.pendingPinTitle

        if (title.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "El titulo es obligatorio")
            return
        }

        if (!hasConfiguredArCoreApiKey()) {
            Log.e(TAG, "No se encontro una ARCORE_API_KEY valida en el manifest")
            _state.value = _state.value.copy(
                hostingState = HostingState.ERROR,
                errorMessage = "Falta ARCORE_API_KEY en la configuracion local o en el manifest"
            )
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
                        errorMessage = getHostingErrorMessage(state)
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
            latitude = location.latitude,
            longitude = location.longitude,
            ttlDays = CloudAnchorManager.DEFAULT_TTL_DAYS
        )

        viewModelScope.launch {
            try {
                repository.savePin(pin)
                _state.value = _state.value.copy(
                    hostingState = HostingState.SUCCESS,
                    isHostingMode = false,
                    pendingPinTitle = "",
                    pendingPinDescription = "",
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

    private fun hasConfiguredArCoreApiKey(): Boolean {
        val apiKey = getArCoreApiKey()
        return apiKey.isNotBlank() &&
            !apiKey.contains("\${") &&
            !apiKey.equals("YOUR_API_KEY", ignoreCase = true)
    }

    private fun getArCoreApiKey(): String {
        return try {
            @Suppress("DEPRECATION")
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.ar.API_KEY").orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "No fue posible leer ARCORE_API_KEY del manifest", e)
            ""
        }
    }

    private fun getHostingErrorMessage(state: Anchor.CloudAnchorState): String {
        return when (state) {
            Anchor.CloudAnchorState.ERROR_NOT_AUTHORIZED ->
                "Error hosting: ERROR_NOT_AUTHORIZED. Revisa la ARCORE_API_KEY, que ARCore API este habilitada y que las restricciones del package/SHA-1 coincidan con esta app."
            Anchor.CloudAnchorState.ERROR_RESOURCE_EXHAUSTED ->
                "Error hosting: ERROR_RESOURCE_EXHAUSTED. Se alcanzo la cuota del servicio de Cloud Anchors."
            Anchor.CloudAnchorState.ERROR_SERVICE_UNAVAILABLE ->
                "Error hosting: ERROR_SERVICE_UNAVAILABLE. Verifica la conexion o la disponibilidad del servicio."
            else -> "Error hosting: ${state.name}"
        }
    }

    private fun getLocationErrorMessage(): String {
        return if (hasLocationPermission()) {
            "No fue posible obtener tu ubicacion actual. Verifica que el GPS este activo e intenta de nuevo."
        } else {
            "Se necesita permiso de ubicacion para guardar el pin con coordenadas."
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
            Log.w(TAG, "Task fallida: ${error.message}")
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
