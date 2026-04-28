package com.example.pinar.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.UserData
import com.example.pinar.ui.MainViewModel

class EditProfileViewModel : ViewModel() {
    private val _state = mutableStateOf(EditProfileState())
    val state: State<EditProfileState> = _state

    fun inicializar(userData: UserData?) {
        userData?.let {
            _state.value = _state.value.copy(
                nombre = it.nombre,
                biografia = it.biografia,
                fotoUrl = it.fotoUrl,
                uid = it.uid
            )
        }
    }

    fun seleccionarFoto(uri: Uri, context: Context) {
        context.contentResolver.getType(uri)
        _state.value = _state.value.copy(fotoUri = uri)
    }

    fun modificarNombre(nombre: String) {
        _state.value = _state.value.copy(nombre = nombre)
    }

    fun modificarBiografia(biografia: String) {
        _state.value = _state.value.copy(biografia = biografia)
    }


}
