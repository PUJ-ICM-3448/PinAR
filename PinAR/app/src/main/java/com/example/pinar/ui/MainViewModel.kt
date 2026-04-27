package com.example.pinar.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.AuthState
import com.example.pinar.data.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.cargando)
    val authState: State<AuthState> = _authState

    private val _userData = mutableStateOf<UserData?>(null)
    val userData: State<UserData?> = _userData

    private val db = FirebaseFirestore.getInstance()

    init {
        verificarAuth()
    }

    fun verificarAuth() {
        if (auth.currentUser != null) {
            val user = auth.currentUser
            user?.uid?.let { uid ->
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        val data = document.toObject(UserData::class.java)
                        _userData.value = data
                        _authState.value = AuthState.autenticado
                    }
            }
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
                    val user = auth.currentUser
                    user?.uid?.let { uid ->
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                val data = document.toObject(UserData::class.java)
                                _userData.value = data
                                _authState.value = AuthState.autenticado
                            }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun registrar(nombre: String, mail: String, contrasena: String, biografia: String) {
        if (mail.isEmpty() || contrasena.isEmpty() || nombre.isEmpty()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        _authState.value = AuthState.cargando
        auth.createUserWithEmailAndPassword(mail, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val nuevoUsuario = UserData(
                        uid = uid,
                        correo = mail,
                        nombre = nombre,
                        biografia = biografia,
                        creacion = Timestamp.now()
                    )
                    
                    db.collection("usuarios").document(uid).set(nuevoUsuario)
                        .addOnSuccessListener {
                            _userData.value = nuevoUsuario
                            _authState.value = AuthState.autenticado
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error("Error al guardar en base de datos")
                        }
                } else {
                    _authState.value = AuthState.Error(tarea.exception?.message ?: "Algo salio mal😧")
                }
            }
    }

    fun cerrar() {
        auth.signOut()
        _userData.value = null
        _authState.value = AuthState.noAutenticado
    }
}
