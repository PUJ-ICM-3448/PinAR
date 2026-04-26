package com.example.pinar.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.AuthState
import com.example.pinar.data.UserData
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.cargando)
    val authState: State<AuthState> = _authState

    private val _userData = mutableStateOf<UserData?>(null)
    val userData: State<UserData?> = _userData

    init {
        verificarAuth()
    }

    fun verificarAuth() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.autenticado
            guardarUsuarioActivo(auth.currentUser?.email ?: "", auth.currentUser?.email?.split("@")[0] ?: "")
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
                    guardarUsuarioActivo(mail, mail.split("@")[0])
                } else {
                    _authState.value =
                        AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
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
                    guardarUsuarioActivo(mail, mail.split("@")[0])
                } else {
                    _authState.value = AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun guardarUsuarioActivo(mail: String, nombre: String) {
        val usuarioNuevo = UserData(mail, nombre)
        guardarUsuario(usuarioNuevo)
    }

    fun guardarUsuario (usuarioNuevo: UserData) {
        _userData.value = usuarioNuevo
    }

    fun cerrar() {
        auth.signOut()
        _authState.value = AuthState.noAutenticado
    }
}
