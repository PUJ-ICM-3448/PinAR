package com.example.pinar.ui.screens.communities

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.LiveLocation
import com.example.pinar.data.UserData
import com.example.pinar.ui.screens.map.util.CustomMapMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityEventScreen(
    communityId: String,
    eventId: String,
    userData: UserData?,
    onBackClick: () -> Unit,
    viewModel: CommunityEventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.628, -74.064), 16f)
    }

    LaunchedEffect(communityId, eventId, userData) {
        viewModel.init(communityId, eventId, userData)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (uiState.isSharingLocation) {
                viewModel.stopSharingLocation()
            }
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
        }
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
                                        onClick = viewModel::startSharingLocation,
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
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(zoomControlsEnabled = true)
                        ) {
                            uiState.liveLocations.forEach { loc ->
                                CustomMapMarker(
                                    imageUrl = loc.photoUrl.ifBlank { null },
                                    fullName = loc.name.ifBlank { "Participante" },
                                    snippet = stringResource(R.string.evento_en_vivo),
                                    location = LatLng(loc.latitude, loc.longitude),
                                    markerColor = MaterialTheme.colorScheme.primary,
                                    onClick = { viewModel.selectParticipant(loc) }
                                )
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
                                    onClick = { /* route handled in v2 */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    enabled = false
                                ) {
                                    Text(stringResource(R.string.evento_trazar_ruta))
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
        Text(
            text = location.name.ifBlank { "Participante" },
            modifier = Modifier.padding(12.dp)
        )
    }
}
