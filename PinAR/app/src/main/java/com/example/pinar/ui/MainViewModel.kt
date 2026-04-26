package com.example.pinar.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.AuthState
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.cargando)
    val authState: State<AuthState> = _authState

    init {
        verificarAuth()
    }

    fun verificarAuth() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.autenticado
        } else {
            _authState.value = AuthState.noAutenticado
        }
    }

    fun login(mail: String, contrasena: String) {
        if (mail.isEmpty() || contrasena.isEmpty()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        _authState.value = AuthState.cargando
        auth.signInWithEmailAndPassword(mail, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    _authState.value = AuthState.autenticado
                } else {
                    _authState.value = AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun registrar(mail: String, contrasena: String) {
        if (mail.isEmpty() || contrasena.isEmpty()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        _authState.value = AuthState.cargando
        auth.createUserWithEmailAndPassword(mail, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    _authState.value = AuthState.autenticado
                } else {
                    _authState.value = AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun cerrar() {
        auth.signOut()
        _authState.value = AuthState.noAutenticado
    }
}
