package com.example.pinar.ui.screens.register

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update


class RegisterViewModel : ViewModel() {
    private val _state = mutableStateOf(RegisterState())
    val state: State<RegisterState> = _state

    fun onNombreChange(nombre: String) {
        _state.value = _state.value.copy(nombre = nombre)
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onBiografiaChange(biografia: String) {
        _state.value = _state.value.copy(biografia = biografia)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onConfirmPasswordChange(password: String) {
        _state.value = _state.value.copy(confirmPassword = password)
    }

    fun seleccionarFoto(uri: Uri, context: Context) {
        context.contentResolver.getType(uri)
        _state.value = _state.value.copy(fotoUri = uri)
    }
}
