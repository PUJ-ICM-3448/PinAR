package com.example.pinar.data

import com.google.ar.core.Session

data class ARSessionState(
    val session: Session? = null,
    val userRequestedInstall: Boolean = true,
    val isInitialized: Boolean = false
)
