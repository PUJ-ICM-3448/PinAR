package com.example.pinar.data

sealed class AuthState {
    object autenticado : AuthState()
    object noAutenticado : AuthState()
    object cargando : AuthState()
    data class Error(val mensaje: String) : AuthState()
}
