package com.example.pinar.ui.screens.newpinlocation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NewPinLocationViewModel : ViewModel() {
    private val _state = mutableStateOf(NewPinLocationState())
    val state: State<NewPinLocationState> = _state

    fun onUbicacionTomar() {
        _state.value = _state.value.copy(ubicacionTomada = true)
    }
}
