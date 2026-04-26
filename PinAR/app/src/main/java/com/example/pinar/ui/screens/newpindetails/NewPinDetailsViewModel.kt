package com.example.pinar.ui.screens.newpindetails

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NewPinDetailsViewModel : ViewModel() {
    private val _state = mutableStateOf(NewPinDetailsState())
    val state: State<NewPinDetailsState> = _state

    fun onTituloChange(titulo: String) {
        if (titulo.length <= 50) {
            _state.value = _state.value.copy(titulo = titulo)
        }
    }

    fun onDescripcionChange(descripcion: String) {
        if (descripcion.length <= 200) {
            _state.value = _state.value.copy(descripcion = descripcion)
        }
    }

    fun onCategoriaSelect(categoria: Categoria) {
        _state.value = _state.value.copy(categoriaSeleccionada = categoria)
    }
}
