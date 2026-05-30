package com.example.pinar.ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.CloudAnchorRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private val repoPines = CloudAnchorRepository()
    val cloudAnchorRepository = CloudAnchorRepository()

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        obtenerPines()
    }

    fun obtenerPines() {
        viewModelScope.launch {
            try {
                val pines = repoPines.getAllPins()
                _state.value = _state.value.copy(lista = pines)
            } catch (e: Exception) {
            }
        }
    }

    fun actualizarVisitas(pinId: String) {
        cloudAnchorRepository.actualizarVisita(pinId)
    }

}