package com.example.pinar.ui.screens.ar

import com.example.pinar.data.ARSessionState
import com.example.pinar.data.CloudAnchorPin
import com.google.ar.core.Anchor
import com.google.ar.core.Session

data class ARState(
    val sessionState: ARSessionState = ARSessionState(),
    val hostingState: HostingState = HostingState.IDLE,
    val featureMapQuality: Session.FeatureMapQuality? = null,
    val resolvedPins: List<ResolvedPin> = emptyList(),
    val availablePins: List<CloudAnchorPin> = emptyList(),
    val errorMessage: String? = null,
    val isHostingMode: Boolean = false,
    val pendingPinTitle: String = "",
    val pendingPinDescription: String = "",
    val selectedCommunityIds: Set<String> = emptySet(),
    val showPinDialog: Boolean = false
)

enum class HostingState {
    IDLE,
    PLACING,
    MAPPING,
    UPLOADING,
    SUCCESS,
    ERROR
}

data class ResolvedPin(
    val anchor: Anchor,
    val pinData: CloudAnchorPin
)
