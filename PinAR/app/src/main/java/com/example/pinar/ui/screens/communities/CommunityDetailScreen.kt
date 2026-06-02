package com.example.pinar.ui.screens.communities

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.R
import com.example.pinar.data.UserData
import com.example.pinar.ui.MainViewModel
import com.example.pinar.ui.screens.home.SeccionHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    communityId: String,
    userData: UserData?,
    mainViewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToPinDetail: (String) -> Unit,
    onNavigateToCreateEvent: (String) -> Unit = {},
    onNavigateToEvent: (String, String) -> Unit = { _, _ -> },
    onNavigateToPinShare: (String) -> Unit = {},
    onNavigateToAR: () -> Unit = {},
    viewModel: CommunityDetailViewModel = viewModel()
) {
    val state by viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val myIds = userData?.memberOf.orEmpty().map { it.id }.toSet()
    val updatedOkMessage = stringResource(R.string.communities_editada_ok)

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editImageUri by remember { mutableStateOf<Uri?>(null) }
    var editIsPublic by remember { mutableStateOf(true) }
    val editImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> editImageUri = uri }

    val isCreator = userData?.uid != null &&
        userData.uid == state.community?.createdBy

    LaunchedEffect(communityId, userData?.memberOf) {
        viewModel.load(communityId, myIds)
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.community?.name ?: stringResource(R.string.communities_detalle),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (isCreator) {
                        IconButton(
                            onClick = {
                                val c = state.community ?: return@IconButton
                                editName = c.name
                                editDescription = c.description
                                editIsPublic = c.isPublic
                                editImageUri = null
                                showEditDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.communities_editar)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            state.community != null -> {
                val community = state.community!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        if (!community.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = community.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = community.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = community.description.ifBlank {
                                stringResource(R.string.communities_sin_descripcion)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_miembros_count, community.memberCount),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.isMember) {
                            Button(
                                onClick = { onNavigateToCreateEvent(communityId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.evento_crear_titulo))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onNavigateToAR,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.crear_pin))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.leaveCommunity(communityId, mainViewModel)
                                },
                                enabled = !state.isJoinLeaveInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.communities_salir))
                            }
                        } else if (community.isPublic) {
                            Button(
                                onClick = {
                                    viewModel.joinCommunity(communityId, mainViewModel)
                                },
                                enabled = !state.isJoinLeaveInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.communities_unirme))
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.communities_privada),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SeccionHeader(titulo = stringResource(R.string.communities_eventos))
                    }
                    if (state.activeEvents.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_eventos_vacio_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.activeEvents, key = { it.id }) { event ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToEvent(communityId, event.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = event.name,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (event.description.isNotBlank()) {
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SeccionHeader(titulo = stringResource(R.string.communities_pines_compartidos))
                    }

                    if (!state.isMember && !community.isPublic) {
                        item {
                            Text(
                                text = stringResource(R.string.communities_pines_solo_miembros),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (state.sharedPins.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.communities_pines_vacio),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.sharedPins, key = { it.id }) { pin ->
                            SharedPinCard(
                                pin = pin,
                                onClick = { onNavigateToPinDetail(pin.id) },
                                onShare = if (state.isMember && pin.createdBy == userData?.uid) {
                                    { onNavigateToPinShare(pin.id) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isUpdating) showEditDialog = false },
            title = { Text(stringResource(R.string.communities_editar)) },
            text = {
                CommunityFormFields(
                    name = editName,
                    onNameChange = { editName = it },
                    description = editDescription,
                    onDescriptionChange = { editDescription = it },
                    imageUri = editImageUri,
                    existingImageUrl = state.community?.imageUrl,
                    onPickImage = { editImagePicker.launch("image/*") },
                    isPublic = editIsPublic,
                    onIsPublicChange = { editIsPublic = it },
                    showPublicToggle = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateCommunity(
                            name = editName,
                            description = editDescription,
                            isPublic = editIsPublic,
                            imageUri = editImageUri,
                            context = context,
                            mainViewModel = mainViewModel
                        ) {
                            showEditDialog = false
                            editImageUri = null
                            scope.launch {
                                snackbarHostState.showSnackbar(updatedOkMessage)
                            }
                        }
                    },
                    enabled = !state.isUpdating
                ) {
                    Text(
                        if (state.isUpdating) "..." else stringResource(R.string.communities_editar_confirmar)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!state.isUpdating) {
                            showEditDialog = false
                            editImageUri = null
                        }
                    },
                    enabled = !state.isUpdating
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}
