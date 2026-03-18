package com.example.pinar.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pinar.R
import com.example.pinar.data.ARSessionState
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import io.github.sceneview.ar.ArSceneView
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import com.example.pinar.ui.utils.LogoVertical


@Composable
fun ARScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.AR,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var sessionState by remember { mutableStateOf(ARSessionState()) }

    if (hasCameraPermission) {
        ARSessionHandler(
            activity = activity,
            context = context,
            lifecycleOwner = lifecycleOwner,
            sessionState = sessionState,
            onSessionStateChange = { sessionState = it }
        )
    }

    ARScreenContent(
        modifier = modifier.fillMaxSize(),
        sessionState = sessionState,
        hasCameraPermission = hasCameraPermission,
        currentScreen = currentScreen,
        onNavigateToHome = onNavigateToHome,
        onNavigateToMap = onNavigateToMap,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToNotifications = onNavigateToNotifications
    )
}

@Composable
fun ARSessionHandler(
    activity: Activity?,
    context: android.content.Context,
    lifecycleOwner: LifecycleOwner,
    sessionState: ARSessionState,
    onSessionStateChange: (ARSessionState) -> Unit
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (activity == null) return@LifecycleEventObserver

                    try {
                        if (sessionState.session == null) {
                            when (ArCoreApk.getInstance().requestInstall(activity, sessionState.userRequestedInstall)) {
                                ArCoreApk.InstallStatus.INSTALLED -> {
                                    val newSession = Session(activity)
                                    onSessionStateChange(
                                        sessionState.copy(
                                            session = newSession,
                                            isInitialized = true
                                        )
                                    )
                                    newSession.resume()
                                }
                                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                                    onSessionStateChange(
                                        sessionState.copy(userRequestedInstall = false)
                                    )
                                    return@LifecycleEventObserver
                                }
                            }
                        } else {
                            sessionState.session?.resume()
                        }
                    } catch (e: UnavailableUserDeclinedInstallationException) {
                        Toast.makeText(context, "Instalación de ARCore fallida", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    sessionState.session?.pause()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sessionState.session?.close()
            onSessionStateChange(sessionState.copy(session = null, isInitialized = false))
        }
    }
}

@Composable
fun ARScreenContent(
    modifier: Modifier = Modifier,
    sessionState: ARSessionState,
    hasCameraPermission: Boolean,
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
                sessionState = sessionState,
                hasCameraPermission = hasCameraPermission
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
    sessionState: ARSessionState,
    hasCameraPermission: Boolean
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            !hasCameraPermission -> {
                Text(text = stringResource(R.string.esperando_permisos_de_c_mara))
            }
            !sessionState.isInitialized -> {
                Text(text = stringResource(R.string.inicializando_ar))
            }
            sessionState.session != null -> {
                Box(modifier = Modifier.fillMaxSize()) {

                    AndroidView(
                        factory = { ctx ->
                            ArSceneView(ctx)
                        }
                    )
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
            else -> {
                Text(text = stringResource(R.string.ar_no_disponible))
            }
        }
    }
}

fun maybeEnableArButton(context: android.content.Context, arButton: Button) {
    val availability = ArCoreApk.getInstance().checkAvailability(context)
    if (availability.isSupported) {
        arButton.visibility = View.VISIBLE
        arButton.isEnabled = true
    } else {
        arButton.visibility = View.INVISIBLE
        arButton.isEnabled = false
    }
}
