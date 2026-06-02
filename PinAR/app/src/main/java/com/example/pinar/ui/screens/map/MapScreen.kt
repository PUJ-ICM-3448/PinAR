package com.example.pinar.ui.screens.map

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pinar.R
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.UserData
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.screens.map.MapViewModel
import com.example.pinar.ui.screens.map.util.CustomMapMarker
import com.example.pinar.ui.screens.map.util.GOOGLE_MAP_CLOUD_ID
import com.example.pinar.ui.screens.map.util.PinMapMarker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Map,
    userData: UserData? = null,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCommunities: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPinDetail: (String) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }
    val memberCommunities = userData?.memberOf.orEmpty()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var isCompassModeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(userData?.uid, memberCommunities) {
        val uid = userData?.uid ?: return@LaunchedEffect
        viewModel.setUserContext(uid, memberCommunities)
    }

    fun focusOnSearchedPin() {
        val query = uiState.searchQuery.trim()
        if (query.isBlank()) return

        val matchedPin = uiState.pins.firstOrNull { it.title.contains(query, ignoreCase = true) }

        if (matchedPin == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar("No se encontró el pin") }
            return
        }

        keyboardController?.hide()
        viewModel.setFollowingUser(false)
        coroutineScope.launch {
            try {
                cameraState.animate(CameraUpdateFactory.newLatLngZoom(matchedPin.position, 19f))
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(cameraState.isMoving) {
        if (cameraState.isMoving && cameraState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            viewModel.setFollowingUser(false)
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        val locationGranted = permissionsState.permissions.any { 
            (it.permission == Manifest.permission.ACCESS_FINE_LOCATION || 
             it.permission == Manifest.permission.ACCESS_COARSE_LOCATION) && it.status.isGranted 
        }
        viewModel.onLocationPermissionChanged(locationGranted)

        val activityGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsState.permissions.any { it.permission == Manifest.permission.ACTIVITY_RECOGNITION && it.status.isGranted }
        } else true
        viewModel.onActivityPermissionChanged(activityGranted)

        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(isCompassModeEnabled) {
        if (!isCompassModeEnabled) onDispose {} else {
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
                                cameraState.move(CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder(cameraState.position).bearing(bearing).tilt(tilt).build()
                                ))
                            }
                        } catch (e: Exception) { Log.e("MapScreen", "Error sensor: ${e.message}") }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            rotationSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    LaunchedEffect(uiState.userLocation, uiState.isFollowingUser) {
        if (uiState.isFollowingUser) {
            uiState.userLocation?.let { location ->
                try {
                    cameraState.animate(CameraUpdateFactory.newLatLngZoom(location, 18f))
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(uiState.routePolyline) {
        if (uiState.routePolyline.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply { uiState.routePolyline.forEach { include(it) } }.build()
            try { cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120)) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(uiState.routeError) {
        uiState.routeError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRouteError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            val hasCommunityFilter = uiState.communityFilter != MapCommunityFilter.ALL
            val hasSearchFilter = uiState.searchQuery.isNotBlank()
            val activeCommunityFilterLabel = when (uiState.communityFilter) {
                MapCommunityFilter.ALL -> null
                MapCommunityFilter.OWN_PINS -> stringResource(R.string.map_filtro_mis_pines)
                MapCommunityFilter.COMMUNITY -> memberCommunities
                    .find { it.id == uiState.selectedCommunityId }
                    ?.name
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.map_filtros_titulo),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            MapFilterChip(
                                label = stringResource(R.string.map_filtro_todos),
                                icon = Icons.Outlined.FilterList,
                                selected = uiState.communityFilter == MapCommunityFilter.ALL,
                                onClick = { viewModel.setCommunityFilter(MapCommunityFilter.ALL) }
                            )
                        }
                        item {
                            MapFilterChip(
                                label = stringResource(R.string.map_filtro_mis_pines),
                                icon = Icons.Outlined.Person,
                                selected = uiState.communityFilter == MapCommunityFilter.OWN_PINS,
                                onClick = { viewModel.setCommunityFilter(MapCommunityFilter.OWN_PINS) }
                            )
                        }
                        items(memberCommunities, key = { it.id }) { community ->
                            MapFilterChip(
                                label = community.name,
                                icon = Icons.Outlined.Groups,
                                selected = uiState.communityFilter == MapCommunityFilter.COMMUNITY
                                    && uiState.selectedCommunityId == community.id,
                                onClick = {
                                    viewModel.setCommunityFilter(MapCommunityFilter.COMMUNITY, community.id)
                                }
                            )
                        }
                    }

                    if (hasCommunityFilter || hasSearchFilter) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeCommunityFilterLabel?.let { label ->
                                item {
                                    ActiveFilterPill(
                                        text = stringResource(R.string.map_filtro_activo, label),
                                        onClear = { viewModel.setCommunityFilter(MapCommunityFilter.ALL) }
                                    )
                                }
                            }
                            if (hasSearchFilter) {
                                item {
                                    ActiveFilterPill(
                                        text = stringResource(R.string.map_busqueda_activa, uiState.searchQuery),
                                        onClear = { viewModel.setSearchQuery("") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.buscar_pin_en_el_mapa)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { IconButton(onClick = ::focusOnSearchedPin) { Icon(Icons.Default.Search, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusOnSearchedPin() }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    googleMapOptionsFactory = {
                        GoogleMapOptions().mapId(GOOGLE_MAP_CLOUD_ID)
                    },
                    properties = MapProperties(
                        isMyLocationEnabled = uiState.hasLocationPermission && uiState.userLocation == null
                    ),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = true, compassEnabled = !isCompassModeEnabled)
                ) {
                    uiState.pins.forEach { pin ->
                        PinMapMarker(
                            title = pin.title,
                            snippet = buildPinSnippet(pin),
                            location = pin.position,
                            onClick = { viewModel.selectPin(pin) }
                        )
                    }
                    
                    uiState.userLocation?.let { location ->
                        CustomMapMarker(
                            imageUrl = userData?.fotoUrl?.ifBlank { null },
                            fullName = userData?.nombre?.ifBlank { stringResource(R.string.mi_ubicacion) }
                                ?: stringResource(R.string.mi_ubicacion),
                            location = location,
                            markerColor = MaterialTheme.colorScheme.tertiary,
                            placeholderResId = R.drawable.profile,
                            onClick = { }
                        )
                    }

                    if (uiState.routePolyline.size >= 2) Polyline(points = uiState.routePolyline, width = 14f)
                }

                Card(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pasos, uiState.stepCount), fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Column(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    FloatingActionButton(
                        onClick = { isCompassModeEnabled = !isCompassModeEnabled },
                        containerColor = if (isCompassModeEnabled) MaterialTheme.colorScheme.primary else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(if (isCompassModeEnabled) Icons.Default.Explore else Icons.Default.Navigation, null, tint = if (isCompassModeEnabled) Color.White else Color.Gray)
                    }
                    
                    FloatingActionButton(
                        onClick = { viewModel.setFollowingUser(true) },
                        containerColor = if (uiState.isFollowingUser) MaterialTheme.colorScheme.primary else Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, null, tint = if (uiState.isFollowingUser) Color.White else Color.Gray)
                    }
                }

                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp))
            }
            Footer(
                currentScreen = currentScreen,
                onHomeClick = onNavigateToHome,
                onMapClick = onNavigateToMap,
                onARClick = onNavigateToAR,
                onCommunitiesClick = onNavigateToCommunities,
                onProfileClick = onNavigateToProfile,
                onNotificationsClick = onNavigateToNotifications
            )
        }
    }

    uiState.selectedPin?.let { selectedPin ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSelectedPin,
            title = { Text(selectedPin.title) },
            text = {
                Column {
                    Text(selectedPin.subtitle)
                    if (selectedPin.visibleCommunityNames.isNotEmpty()) {
                        Text(
                            text = selectedPin.visibleCommunityNames.joinToString(", "),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (uiState.userLocation == null) {
                        Text(
                            text = stringResource(R.string.quieres_trazar_una_ruta_desde_tu_ubicacion_actual),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::fetchRouteToSelectedPin,
                    enabled = !uiState.isLoadingRoute && uiState.userLocation != null
                ) {
                    if (uiState.isLoadingRoute) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.trazar_ruta))
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onNavigateToPinDetail(selectedPin.id) }) {
                        Text(stringResource(R.string.ver_detalle))
                    }
                    TextButton(onClick = viewModel::dismissSelectedPin) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            }
        )
    }
}

@Composable
private fun MapFilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                tint = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ActiveFilterPill(
    text: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

private fun buildPinSnippet(pin: com.example.pinar.data.PinMapItem): String {
    val communities = pin.visibleCommunityNames.joinToString(", ")
    return if (communities.isNotBlank()) "$communities · ${pin.subtitle}" else pin.subtitle
}
