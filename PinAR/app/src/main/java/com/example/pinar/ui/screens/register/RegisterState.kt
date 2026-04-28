package com.example.pinar.ui.screens.register

import android.net.Uri


data class RegisterState(
    val nombre: String = "",
    val email: String = "",
    val biografia: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fotoUri: Uri? = null
)
