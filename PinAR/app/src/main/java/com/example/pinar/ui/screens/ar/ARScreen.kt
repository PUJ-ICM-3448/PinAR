package com.example.pinar.ui.screens.ar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.utils.LogoVertical
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode

@Composable
fun ARScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.AR,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPins: () -> Unit = {},
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
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Active el acceso a cámara para AR", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!hasLocationPermission) {
            Toast.makeText(
                context,
                "Se requiere permiso de ubicacion para guardar coordenadas precisas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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

    LaunchedEffect(state.hostingState) {
        if (state.hostingState == HostingState.SUCCESS) {
            Toast.makeText(context, "Pin guardado exitosamente", Toast.LENGTH_SHORT).show()
            viewModel.resetHostingState()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ARCameraView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                viewModel = viewModel,
                state = state,
                hasCameraPermission = hasCameraPermission,
                onNavigateToPins = onNavigateToPins
            )

            Footer(
                currentScreen = currentScreen,
                unreadCount = 3, //cambiar a futuro
                onHomeClick = onNavigateToHome,
                onMapClick = onNavigateToMap,
                onARClick = {},
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }

        if (state.showPinDialog) {
            PinDetailsDialog(
                title = state.pendingPinTitle,
                description = state.pendingPinDescription,
                onTitleChange = { viewModel.onPinTitleChange(it) },
                onDescriptionChange = { viewModel.onPinDescriptionChange(it) },
                onConfirm = { viewModel.confirmAndHostPin() },
                onDismiss = { viewModel.cancelHosting() }
            )
        }

        state.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("Ok")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun ARCameraView(
    modifier: Modifier = Modifier,
    viewModel: ARViewModel,
    state: ARState,
    hasCameraPermission: Boolean,
    onNavigateToPins: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var arView by remember { mutableStateOf<ArSceneView?>(null) }
    var sessionConfigured by remember { mutableStateOf(false) }
    val renderedResolvedPinIds = remember { mutableSetOf<String>() }

    //para touch listener
    var currentHostingMode by remember { mutableStateOf(false) }
    var currentHostingState by remember { mutableStateOf(HostingState.IDLE) }
    LaunchedEffect(state.isHostingMode, state.hostingState) {
        currentHostingMode = state.isHostingMode
        currentHostingState = state.hostingState
    }

    LaunchedEffect(state.resolvedPins, arView) {
        val view = arView ?: return@LaunchedEffect
        state.resolvedPins.forEach { resolvedPin ->
            val cloudAnchorId = resolvedPin.pinData.cloudAnchorId
            if (cloudAnchorId.isNotBlank() && renderedResolvedPinIds.add(cloudAnchorId)) {
                val modelNode = ArModelNode(
                    engine = view.engine
                ).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/map_pin_location_pin.glb",
                        autoAnimate = true,
                        scaleToUnits = 0.5f
                    )
                    anchor = resolvedPin.anchor
                }
                view.addChild(modelNode)
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            !hasCameraPermission -> Text(stringResource(R.string.esperando_permisos_de_c_mara))
            !state.sessionState.isInitialized -> Text(stringResource(R.string.inicializando_ar))
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            ArSceneView(ctx).also { view ->
                                arView = view
                                view.onArFrame = { frame ->
                                    val arSession = view.arSession
                                    if (arSession != null) {
                                        if (viewModel.state.value.sessionState.session == null) {
                                            viewModel.onSessionCreated(arSession)
                                        }
                                        if (viewModel.state.value.hostingState == HostingState.MAPPING) {//en general, evita que se trabe
                                            val config = arSession.config
                                            if (config.cloudAnchorMode != Config.CloudAnchorMode.ENABLED ||
                                                config.planeFindingMode == Config.PlaneFindingMode.DISABLED) {
                                                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                                                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                                                config.focusMode = Config.FocusMode.AUTO
                                                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                                                arSession.configure(config)
                                            }
                                            frame.camera?.pose?.let { cameraPose ->
                                                viewModel.updateFeatureMapQuality(cameraPose)
                                            }
                                        }
                                    }
                                }

                                //Touch listener
                                @android.annotation.SuppressLint("ClickableViewAccessibility")
                                view.setOnTouchListener { _, event ->
                                    if (event.action == MotionEvent.ACTION_UP &&
                                        viewModel.state.value.isHostingMode &&
                                        viewModel.state.value.hostingState == HostingState.PLACING
                                    ) {
                                        view.arSession?.update()?.let { frame ->
                                            try {
                                                val hits = frame.hitTest(event) //hitTest es la posicion, hitResult es confirmada
                                                hits.firstOrNull { hit ->
                                                    val trackable = hit.trackable
                                                    trackable is Plane &&
                                                        trackable.isPoseInPolygon(hit.hitPose)
                                                }?.let { hitResult ->
                                                    val anchor = hitResult.createAnchor()
                                                    val modelNode = ArModelNode(
                                                        engine = view.engine
                                                    ).apply {
                                                        loadModelGlbAsync(
                                                            glbFileLocation = "models/map_pin_location_pin.glb",
                                                            autoAnimate = true,
                                                            scaleToUnits = 0.5f
                                                        )
                                                        this.anchor = anchor
                                                    }
                                                    view.addChild(modelNode)
                                                    viewModel.onPlaneTapped(hitResult)
                                                }
                                            } catch (e: Exception) {
                                                Log.e("ARScreen", "error en hitTest", e)
                                            }
                                        }
                                    }
                                    false
                                }
                            }
                        },
                        update = { _ -> }
                    )

                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> arView?.arSession?.resume()
                                Lifecycle.Event.ON_PAUSE -> arView?.arSession?.pause()
                                 Lifecycle.Event.ON_DESTROY -> {
                                    renderedResolvedPinIds.clear()
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

                    AROverlay(
                        state = state,
                        onPublishClick = { viewModel.startHostingMode() },
                        onViewPinsClick = onNavigateToPins,
                        onCancelHosting = { viewModel.cancelHosting() },
                        onShowPinDialog = { viewModel.showPinDetailsDialog() }
                    )
                }
            }
        }
    }
}

@Composable
fun AROverlay(
    state: ARState,
    onPublishClick: () -> Unit,
    onViewPinsClick: () -> Unit,
    onCancelHosting: () -> Unit,
    onShowPinDialog: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !state.isHostingMode -> {
                Button(
                    onClick = onViewPinsClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.ver_pines))
                }

                if (state.resolvedPins.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pines_visibles, state.resolvedPins.size),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                Button(
                    onClick = onPublishClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    LogoVertical(icono = R.drawable.camera, text = stringResource(R.string.publicar))
                }
            }
            state.hostingState == HostingState.PLACING -> {
                HostingInstructions(
                    text = stringResource(R.string.toca_un_plano_detectado_puntos_para_colocar_el_pin),
                    onCancel = onCancelHosting
                )
            }

            state.hostingState == HostingState.MAPPING -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.pin_colocado),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.mueve_el_celular_alrededor_del_pin_para_mejorar_el_mapeo),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureQualityIndicator(quality = state.featureMapQuality)
                }
                IconButton(
                    onClick = onCancelHosting,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancelar), tint = Color.White)
                }

                val habilitarConfrimado = state.featureMapQuality != null && state.featureMapQuality != Session.FeatureMapQuality.INSUFFICIENT
                Button(
                    onClick = onShowPinDialog,
                    enabled = habilitarConfrimado,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (habilitarConfrimado) Color(0xFF4CAF50) else Color.Gray
                    )
                ) {
                    Text(stringResource(R.string.confirmar_posici_n_pin))
                }
            }
            state.hostingState == HostingState.UPLOADING -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.subiendo_pin_a_la_nube),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            state.hostingState == HostingState.ERROR -> {
                HostingInstructions(
                    text = stringResource(R.string.error_al_subir_el_pin_intente_de_nuevo),
                    onCancel = onCancelHosting
                )
            }
        }
    }
}

@Composable
fun FeatureQualityIndicator(quality: Session.FeatureMapQuality?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.calidad), color = Color.White, fontSize = 13.sp)

        val (color, label) = when (quality) {
            Session.FeatureMapQuality.INSUFFICIENT -> Color(0xFFF44336) to stringResource(R.string.insuficiente)
            Session.FeatureMapQuality.SUFFICIENT -> Color(0xFFFF9800) to stringResource(R.string.suficiente)
            Session.FeatureMapQuality.GOOD -> Color(0xFF4CAF50) to stringResource(R.string.buena)
            else -> Color.Gray to stringResource(R.string.evaluando)
        }

        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HostingInstructions(text: String, onCancel: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancelar), tint = Color.White)
        }
    }
}

@Composable
fun PinDetailsDialog(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nuevo_pin)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.t_tulo)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.descripci_n)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.publicar_pin))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
