package com.example.pinar.ui.screens.pins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CloudAnchorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinListViewModel : ViewModel() {
    private val repository = CloudAnchorRepository()

    private val _state = MutableStateFlow(PinListState())
    val state: StateFlow<PinListState> = _state.asStateFlow()

    init {
        loadPins()
    }

    fun loadPins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pins = repository.getAllPins().sortedByDescending { it.fecha }
                _state.update {
                    it.copy(
                        isLoading = false,
                        pins = pins,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "No fue posible cargar los pines"
                    )
                }
            }
        }
    }
}
