package com.example.pinar.ui.screens.ar

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.ARSessionState

class ARViewModel : ViewModel() {
    private val _state = mutableStateOf(ARState())
    val state: State<ARState> = _state

    fun onSessionStateChange(newState: ARSessionState) {
        _state.value = _state.value.copy(sessionState = newState)
    }
}
