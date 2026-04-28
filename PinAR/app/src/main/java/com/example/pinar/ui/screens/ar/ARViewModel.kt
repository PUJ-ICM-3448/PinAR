package com.example.pinar.ui.screens.ar

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.ar.CloudAnchorManager
import com.example.pinar.data.ARSessionState
import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.CloudAnchorRepository
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla AR.
 * Maneja el ciclo de vida de Cloud Anchors: hosting, resolving y persistencia en Firestore.
 *
 * Ref: https://developers.google.com/ar/develop/java/cloud-anchors/developer-guide
 */
class ARViewModel : ViewModel() {
    private val _state = mutableStateOf(ARState())
    val state: State<ARState> = _state

    private val cloudAnchorManager = CloudAnchorManager()
    private val repository = CloudAnchorRepository()

    // Anchor local creado por hit-test, antes de subir a la nube
    private var localAnchor: Anchor? = null

    companion object {
        private const val TAG = "ARViewModel"
    }

    // ─── Manejo de sesión ─────────────────────────────────────────────

    fun onSessionStateChange(newState: ARSessionState) {
        _state.value = _state.value.copy(sessionState = newState)
    }

    /**
     * Llamado cuando la sesión ARCore se crea.
     * Habilita Cloud Anchors en la configuración.
     */
    fun onSessionCreated(session: Session) {
        cloudAnchorManager.enableCloudAnchors(session)
        _state.value = _state.value.copy(
            sessionState = _state.value.sessionState.copy(session = session)
        )
        Log.d(TAG, "Sesión creada y Cloud Anchors habilitados")
    }

    // ─── Flujo de Hosting (Crear Pin) ─────────────────────────────────

    /**
     * Activa el modo hosting: el usuario puede tocar un plano para colocar un pin.
     */
    fun startHostingMode() {
        _state.value = _state.value.copy(
            isHostingMode = true,
            hostingState = HostingState.PLACING,
            errorMessage = null
        )
    }

    /**
     * Cancela el modo hosting y limpia el estado.
     */
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

    /**
     * Llamado cuando el usuario toca un plano detectado.
     * Crea un anchor local en el punto de impacto.
     */
    fun onPlaneTapped(hitResult: HitResult) {
        if (_state.value.hostingState != HostingState.PLACING) return

        val trackable = hitResult.trackable
        if (trackable !is Plane || !trackable.isPoseInPolygon(hitResult.hitPose)) return

        // Limpiar anchor anterior si existe
        localAnchor?.detach()

        // Crear anchor local en el punto tocado
        localAnchor = hitResult.createAnchor()
        _state.value = _state.value.copy(hostingState = HostingState.MAPPING)
        Log.d(TAG, "Anchor local creado. Mapeando entorno...")
    }

    /**
     * Evalúa la calidad del mapping. Llamar periódicamente durante MAPPING.
     */
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

    /**
     * Sube el anchor a la nube (async) y guarda los datos en Firestore al completar.
     *
     * Usa hostCloudAnchorAsync con BiConsumer callback (ARCore 1.33+).
     */
    fun confirmAndHostPin() {
        val session = _state.value.sessionState.session ?: return
        val anchor = localAnchor ?: return
        val title = _state.value.pendingPinTitle

        if (title.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "El título es obligatorio")
            return
        }

        _state.value = _state.value.copy(
            hostingState = HostingState.UPLOADING,
            showPinDialog = false,
            errorMessage = null
        )
        Log.d(TAG, "Hosting iniciado para pin: $title")

        // Llamada asíncrona — el callback llega cuando ARCore termina el hosting
        cloudAnchorManager.hostCloudAnchor(session, anchor) { cloudAnchorId, state ->
            if (state == Anchor.CloudAnchorState.SUCCESS && cloudAnchorId != null) {
                Log.d(TAG, "Hosting exitoso. Cloud Anchor ID: $cloudAnchorId")
                savePinToFirestore(cloudAnchorId)
            } else {
                Log.e(TAG, "Error hosting: $state")
                _state.value = _state.value.copy(
                    hostingState = HostingState.ERROR,
                    errorMessage = "Error hosting: ${state.name}"
                )
            }
        }
    }

    private fun savePinToFirestore(cloudAnchorId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val pin = CloudAnchorPin(
            cloudAnchorId = cloudAnchorId,
            title = _state.value.pendingPinTitle,
            description = _state.value.pendingPinDescription,
            buildingId = "default", // TODO: Permitir seleccionar edificio
            floor = 1,
            createdBy = currentUser?.uid ?: "anonymous",
            latitude = 0.0,  // TODO: Obtener GPS aproximado
            longitude = 0.0,
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

    // ─── Flujo de Resolving (Ver Pines) ──────────────────────────────

    /**
     * Carga los pines de Firestore y comienza a resolverlos con callbacks async.
     */
    fun resolveNearbyPins() {
        val session = _state.value.sessionState.session ?: return

        viewModelScope.launch {
            try {
                val pins = repository.getAllPins()
                _state.value = _state.value.copy(availablePins = pins)
                Log.d(TAG, "Cargados ${pins.size} pines de Firestore")

                for (pin in pins) {
                    if (pin.cloudAnchorId.isNotEmpty()) {
                        cloudAnchorManager.resolveCloudAnchor(
                            session,
                            pin.cloudAnchorId
                        ) { anchor, state ->
                            if (state == Anchor.CloudAnchorState.SUCCESS && anchor != null) {
                                val resolvedPin = ResolvedPin(anchor = anchor, pinData = pin)
                                _state.value = _state.value.copy(
                                    resolvedPins = _state.value.resolvedPins + resolvedPin
                                )
                                Log.d(TAG, "Pin resuelto: ${pin.title}")
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

    // ─── Limpieza ────────────────────────────────────────────────────

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun resetHostingState() {
        _state.value = _state.value.copy(hostingState = HostingState.IDLE)
    }

    override fun onCleared() {
        super.onCleared()
        localAnchor?.detach()
    }
}
