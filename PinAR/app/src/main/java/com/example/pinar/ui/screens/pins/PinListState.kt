package com.example.pinar.ui.screens.pins

import com.example.pinar.data.CloudAnchorPin

data class PinListState(
    val isLoading: Boolean = true,
    val pins: List<CloudAnchorPin> = emptyList(),
    val error: String? = null
)
