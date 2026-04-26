package com.example.pinar.ui.screens.newpindetails

data class NewPinDetailsState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoriaSeleccionada: Categoria? = null
)
