package com.example.pinar.ui.screens.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.UserData
import com.example.pinar.ui.MainViewModel
import kotlinx.coroutines.launch

class ProfileViewModel: ViewModel() {
    private val repoPines = CloudAnchorRepository()
    val cloudAnchorRepository = CloudAnchorRepository()

    val mainViewModel = MainViewModel()


    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    fun inicializar(userData: UserData?) {
        userData?.let {
            _state.value = _state.value.copy(
                uid = userData.uid
            )
        }
        obtenerPines()
        obtenerContadorComentarios()
    }

    fun obtenerPines() {
        viewModelScope.launch {
            try {
                val pines = repoPines.getAllPins().filter { it.createdBy == _state.value.uid }
                _state.value = _state.value.copy(lista = pines)
            } catch (e: Exception) {
            }
        }
    }

    private fun obtenerContadorComentarios() {
        viewModelScope.launch {
            val num = mainViewModel.numComentarios(state.value.uid)
            _state.value = _state.value.copy(comentarios = num)
        }
    }

}