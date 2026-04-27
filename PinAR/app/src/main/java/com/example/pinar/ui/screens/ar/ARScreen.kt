package com.example.pinar.ui.screens.ar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.ARSessionState
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.utils.LogoVertical
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import io.github.sceneview.ar.ArSceneView

@Composable
fun ARScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.AR,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    viewModel: ARViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Se requiere permiso de cámara para AR", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gestionar instalación de ARCore
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission && activity != null && !state.sessionState.isInitialized) {
            try {
                val installStatus = ArCoreApk.getInstance().requestInstall(activity, state.sessionState.userRequestedInstall)
                if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {
                    viewModel.onSessionStateChange(state.sessionState.copy(isInitialized = true))
                }
            } catch (_: Exception) {
                Toast.makeText(context, "ARCore es necesario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ARScreenContent(
        modifier = modifier.fillMaxSize(),
        sessionState = state.sessionState,
        hasCameraPermission = hasCameraPermission,
        onSessionCreated = { session ->
            if (state.sessionState.session != session) {
                viewModel.onSessionStateChange(state.sessionState.copy(session = session))
            }
        },
        currentScreen = currentScreen,
        onNavigateToHome = onNavigateToHome,
        onNavigateToMap = onNavigateToMap,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToNotifications = onNavigateToNotifications
    )
}

@Composable
fun ARScreenContent(
    modifier: Modifier = Modifier,
    sessionState: ARSessionState,
    hasCameraPermission: Boolean,
    onSessionCreated: (Session) -> Unit,
    currentScreen: Screen = Screen.AR,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            ARCameraView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                isInitialized = sessionState.isInitialized,
                hasCameraPermission = hasCameraPermission,
                onSessionCreated = onSessionCreated
            )

            Footer(
                currentScreen = currentScreen,
                unreadCount = 3,
                onHomeClick = onNavigateToHome,
                onMapClick = onNavigateToMap,
                onARClick = {},
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }
    }
}

@Composable
fun ARCameraView(
    modifier: Modifier = Modifier,
    isInitialized: Boolean,
    hasCameraPermission: Boolean,
    onSessionCreated: (Session) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var arView by remember { mutableStateOf<ArSceneView?>(null) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            !hasCameraPermission -> Text(stringResource(R.string.esperando_permisos_de_c_mara))
            !isInitialized -> Text(stringResource(R.string.inicializando_ar))
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            ArSceneView(ctx).also { arView = it }
                        },
                        update = { view ->
                            // En 0.10.0, arSession ya es la sesión de ARCore (hereda de Session)
                            view.arSession?.let { arSession ->
                                onSessionCreated(arSession)
                                
                                // Configuración de rendimiento (sin lag)
                                if (arSession.config.updateMode != Config.UpdateMode.LATEST_CAMERA_IMAGE) {
                                    arSession.configure { config ->
                                        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                                        config.focusMode = Config.FocusMode.AUTO
                                    }
                                }
                            }
                        }
                    )

                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> arView?.arSession?.resume()
                                Lifecycle.Event.ON_PAUSE -> arView?.arSession?.pause()
                                Lifecycle.Event.ON_DESTROY -> {
                                    arView?.arSession?.destroy()
                                    arView = null
                                }
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { },
                            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
                        ) {
                            Text(stringResource(R.string.ver_pines))
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                        ) {
                            LogoVertical(icono = R.drawable.camera, text = stringResource(R.string.publicar))
                        }
                    }
                }
            }
        }
    }
}
