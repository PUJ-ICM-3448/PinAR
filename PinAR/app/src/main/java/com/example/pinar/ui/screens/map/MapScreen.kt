package com.example.pinar.ui.screens.map

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.screens.map.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Map,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    var isCompassModeEnabled by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    DisposableEffect(isCompassModeEnabled) {
        if (!isCompassModeEnabled) {
            onDispose {}
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        try {
                            val rotationMatrix = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            
                            val bearing = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                            val tilt = (-pitch).coerceIn(0f, 67.5f)

                            if (!cameraState.isMoving) {
                                cameraState.move(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.builder(cameraState.position)
                                            .bearing(bearing)
                                            .tilt(tilt)
                                            .build()
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MapScreen", "Error actualizando orientación: ${e.message}")
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (rotationSensor != null) {
                sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        viewModel.onLocationPermissionChanged(locationPermissions.allPermissionsGranted)
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(uiState.routeError) {
        uiState.routeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRouteError()
        }
    }

    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(location, 18f))
        }
    }

    LaunchedEffect(uiState.routePolyline) {
        if (uiState.routePolyline.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                uiState.routePolyline.forEach { include(it) }
            }.build()
            cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar pin en el mapa") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (!locationPermissions.allPermissionsGranted) {
                Text(
                    text = "Habilita ubicacion para trazar rutas desde tu posicion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissions.allPermissionsGranted
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = locationPermissions.allPermissionsGranted,
                        zoomControlsEnabled = true,
                        compassEnabled = !isCompassModeEnabled
                    )
                ) {
                    uiState.pins
                        .filter { pin ->
                            searchQuery.isBlank() ||
                                pin.title.contains(searchQuery, ignoreCase = true) ||
                                pin.subtitle.contains(searchQuery, ignoreCase = true)
                        }
                        .forEach { pin ->
                            Marker(
                                state = MarkerState(position = pin.position),
                                title = pin.title,
                                snippet = pin.subtitle,
                                onClick = {
                                    viewModel.selectPin(pin)
                                    true
                                }
                            )
                        }
                    uiState.userLocation?.let { position ->
                        Marker(
                            state = MarkerState(position = position),
                            title = "Mi ubicacion"
                        )
                    }
                    if (uiState.routePolyline.size >= 2) {
                        Polyline(
                            points = uiState.routePolyline,
                            width = 14f
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { isCompassModeEnabled = !isCompassModeEnabled },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = if (isCompassModeEnabled) MaterialTheme.colorScheme.primary else Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isCompassModeEnabled) Icons.Default.Explore else Icons.Default.Navigation,
                        contentDescription = "Modo Brújula",
                        tint = if (isCompassModeEnabled) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                )
            }
            Footer(
                currentScreen = currentScreen,
                unreadCount = 3,
                onHomeClick = onNavigateToHome,
                onMapClick = onNavigateToMap,
                onARClick = onNavigateToAR,
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }
        if (uiState.routePolyline.isNotEmpty()) {
            TextButton(
                onClick = viewModel::clearRoute,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 92.dp, end = 20.dp)
            ) {
                Text("Limpiar ruta", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    uiState.selectedPin?.let { selectedPin ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSelectedPin,
            title = { Text(selectedPin.title) },
            text = { Text("Quieres trazar una ruta desde tu ubicacion actual?") },
            confirmButton = {
                Button(
                    onClick = viewModel::fetchRouteToSelectedPin,
                    enabled = !uiState.isLoadingRoute
                ) {
                    if (uiState.isLoadingRoute) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Trazar ruta")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSelectedPin) {
                    Text("Cancelar")
                }
            }
        )
    }
}
