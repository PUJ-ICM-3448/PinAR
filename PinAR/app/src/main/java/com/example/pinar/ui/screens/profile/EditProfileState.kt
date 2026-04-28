package com.example.pinar.ui.screens.profile

import android.net.Uri

data class EditProfileState(
    val nombre: String = "",
    val biografia: String = "",
    val fotoUrl: String = "",
    val uid: String = "",
    val fotoUri: Uri? = null
)
