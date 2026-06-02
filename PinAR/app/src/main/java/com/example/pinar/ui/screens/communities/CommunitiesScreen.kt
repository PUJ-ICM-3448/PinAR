package com.example.pinar.ui.screens.communities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.UserData
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.MainViewModel
import com.example.pinar.ui.screens.home.SeccionHeader
import com.example.pinar.ui.utils.Footer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    userData: UserData?,
    mainViewModel: MainViewModel,
    currentScreen: Screen = Screen.Communities,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCommunityDetail: (String) -> Unit,
    viewModel: CommunitiesViewModel = viewModel()
) {
    val state by viewModel.state
    val myCommunities = userData?.memberOf.orEmpty()
    val myIds = myCommunities.map { it.id }.toSet()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val createdOkMessage = stringResource(R.string.communities_creada_ok)

    var showCreateDialog by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var createDescription by remember { mutableStateOf("") }

    LaunchedEffect(userData?.memberOf) {
        viewModel.syncMyCommunities(myCommunities)
        viewModel.loadRecommended(myIds)
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
                        text = stringResource(R.string.communities_titulo),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.communities_crear))
            }
        },
        bottomBar = {
            Footer(
                currentScreen = currentScreen,
                onHomeClick = onNavigateToHome,
                onMapClick = onNavigateToMap,
                onARClick = onNavigateToAR,
                onCommunitiesClick = {},
                onProfileClick = onNavigateToProfile
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.communities_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SeccionHeader(titulo = stringResource(R.string.home_mis_comunidades))
            }

            if (myCommunities.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.communities_mis_vacio),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(myCommunities, key = { it.id }) { community ->
                    community.toListCard {
                        onNavigateToCommunityDetail(community.id)
                    }
                }
            }

            item {
                SeccionHeader(titulo = stringResource(R.string.home_recomendadas_titulo))
            }

            when {
                state.isLoadingRecommended -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.recommendedError != null -> {
                    item {
                        Text(
                            text = state.recommendedError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                state.recommended.isEmpty() -> {
                    item {
                        Text(
                            text = stringResource(R.string.communities_recomendadas_vacio),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                else -> {
                    items(state.recommended, key = { it.id }) { community ->
                        community.toListCard {
                            onNavigateToCommunityDetail(community.id)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isCreating) showCreateDialog = false },
            title = { Text(stringResource(R.string.communities_crear)) },
            text = {
                androidx.compose.foundation.layout.Column {
                    OutlinedTextField(
                        value = createName,
                        onValueChange = { createName = it },
                        label = { Text(stringResource(R.string.communities_nombre)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = createDescription,
                        onValueChange = { createDescription = it },
                        label = { Text(stringResource(R.string.communities_descripcion)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createCommunity(createName, createDescription, mainViewModel) { id ->
                            showCreateDialog = false
                            createName = ""
                            createDescription = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(createdOkMessage)
                            }
                            onNavigateToCommunityDetail(id)
                        }
                    },
                    enabled = !state.isCreating
                ) {
                    Text(if (state.isCreating) "..." else stringResource(R.string.communities_crear_confirmar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
                    enabled = !state.isCreating
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}
