package com.example.pinar.ui.screens.communities

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.LiveLocation
import com.example.pinar.data.UserData
import com.example.pinar.ui.screens.map.util.CustomMapMarker
import com.example.pinar.ui.screens.map.util.GOOGLE_MAP_CLOUD_ID
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CommunityEventScreen(
    communityId: String,
    eventId: String,
    userData: UserData?,
    onBackClick: () -> Unit,
    viewModel: CommunityEventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.628, -74.064), 12f)
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val locationGranted = permissionsState.permissions.any {
        (it.permission == Manifest.permission.ACCESS_FINE_LOCATION ||
            it.permission == Manifest.permission.ACCESS_COARSE_LOCATION) && it.status.isGranted
    }
    var pendingShare by remember { mutableStateOf(false) }

    LaunchedEffect(locationGranted, pendingShare) {
        if (locationGranted && pendingShare) {
            pendingShare = false
            viewModel.onShareLocationClicked(true)
        }
    }

    LaunchedEffect(locationGranted, uiState.isSharingLocation) {
        if (locationGranted && uiState.isSharingLocation) {
            viewModel.ensureLocationUpdatesIfSharing()
        }
    }

    LaunchedEffect(communityId, eventId, userData) {
        viewModel.init(communityId, eventId, userData)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.routeError) {
        uiState.routeError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearRouteError()
        }
    }

    LaunchedEffect(uiState.routePolyline) {
        if (uiState.routePolyline.size >= 2) {
            val bounds = uiState.routePolyline.fold(LatLngBounds.builder()) { builder, point ->
                builder.include(point)
            }.build()
            try {
                cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.event?.name ?: stringResource(R.string.evento_detalle))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.event == null -> {
                Text(
                    text = uiState.error ?: stringResource(R.string.evento_no_encontrado),
                    modifier = Modifier.padding(padding).padding(24.dp)
                )
            }

            else -> {
                val event = uiState.event!!
                val isActive = event.isActive

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = if (isActive) {
                                stringResource(R.string.evento_activo)
                            } else {
                                stringResource(R.string.evento_finalizado)
                            },
                            color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = stringResource(
                                R.string.evento_participantes_count,
                                event.participants.size
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (isActive) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!uiState.isSharingLocation) {
                                    Button(
                                        onClick = {
                                            if (locationGranted) {
                                                viewModel.onShareLocationClicked(true)
                                            } else {
                                                pendingShare = true
                                                permissionsState.launchMultiplePermissionRequest()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(stringResource(R.string.evento_compartir_ubicacion))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = viewModel::stopSharingLocation,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(stringResource(R.string.evento_detener_ubicacion))
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraState,
                            googleMapOptionsFactory = {
                                GoogleMapOptions().mapId(GOOGLE_MAP_CLOUD_ID)
                            },
                            properties = MapProperties(isMyLocationEnabled = locationGranted),
                            uiSettings = MapUiSettings(zoomControlsEnabled = true)
                        ) {
                            uiState.userLocation?.let { myLoc ->
                                if (uiState.isSharingLocation) {
                                    CustomMapMarker(
                                        imageUrl = userData?.fotoUrl?.ifBlank { null },
                                        fullName = userData?.nombre?.ifBlank { stringResource(R.string.evento_tu_ubicacion) }
                                            ?: stringResource(R.string.evento_tu_ubicacion),
                                        snippet = stringResource(R.string.evento_compartiendo),
                                        location = myLoc,
                                        markerColor = MaterialTheme.colorScheme.tertiary,
                                        placeholderResId = R.drawable.profile,
                                        onClick = { }
                                    )
                                }
                            }
                            uiState.liveLocations.forEach { loc ->
                                CustomMapMarker(
                                    imageUrl = loc.photoUrl.ifBlank { null },
                                    fullName = loc.name.ifBlank { "Participante" },
                                    snippet = stringResource(R.string.evento_en_vivo),
                                    location = LatLng(loc.latitude, loc.longitude),
                                    markerColor = MaterialTheme.colorScheme.primary,
                                    placeholderResId = R.drawable.profile,
                                    onClick = { viewModel.selectParticipant(loc) }
                                )
                            }
                            if (uiState.routePolyline.size >= 2) {
                                Polyline(points = uiState.routePolyline, width = 14f)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.35f)
                            .padding(horizontal = 20.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.evento_participantes_activos),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (uiState.liveLocations.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.evento_sin_ubicaciones),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(uiState.liveLocations, key = { it.uid }) { loc ->
                                ParticipantRow(loc) { viewModel.selectParticipant(loc) }
                            }
                        }
                    }

                    uiState.selectedParticipant?.let { participant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = participant.name.ifBlank { "Participante" },
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = viewModel::fetchRouteToParticipant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    enabled = !uiState.isLoadingRoute &&
                                        uiState.userLocation != null &&
                                        uiState.selectedParticipant != null
                                ) {
                                    if (uiState.isLoadingRoute) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(stringResource(R.string.evento_trazar_ruta))
                                    }
                                }
                                OutlinedButton(
                                    onClick = viewModel::dismissParticipant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.cancelar))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(location: LiveLocation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = location.photoUrl.ifBlank { null },
                contentDescription = location.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.profile),
                error = painterResource(R.drawable.profile)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = location.name.ifBlank { "Participante" })
        }
    }
}
