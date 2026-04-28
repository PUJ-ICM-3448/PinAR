package com.example.pinar.ui.screens.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.UserData

class EditProfileViewModel : ViewModel() {
    private val _state = mutableStateOf(EditProfileState())
    val state: State<EditProfileState> = _state

    fun inicializar(userData: UserData?) {
        userData?.let {
            _state.value = _state.value.copy(
                nombre = it.nombre,
                biografia = it.biografia,
                fotoUrl = it.fotoUrl
            )
        }
    }

    fun modificarNombre(nombre: String) {
        _state.value = _state.value.copy(nombre = nombre)
    }

    fun modificarBiografia(biografia: String) {
        _state.value = _state.value.copy(biografia = biografia)
    }
}
