package com.example.pinar.ui.screens.communities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinShareScreen(
    pinId: String,
    userData: UserData?,
    onBackClick: () -> Unit,
    viewModel: PinShareViewModel = viewModel()
) {
    val state by viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val communities = userData?.memberOf.orEmpty()

    LaunchedEffect(pinId, communities) {
        viewModel.load(pinId, communities)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pin_share_titulo)) },
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
            state.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            communities.isEmpty() -> {
                Text(
                    text = stringResource(R.string.pin_share_sin_comunidades),
                    modifier = Modifier.padding(padding).padding(24.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = state.pinTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.pin_share_subtitulo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(communities, key = { it.id }) { community ->
                        CommunityShareRow(
                            community = community,
                            alreadyShared = community.id in state.sharedCommunityIds,
                            enabled = !state.isSharing,
                            onShare = { viewModel.shareWithCommunity(community.id, pinId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityShareRow(
    community: CommunityBasicInfo,
    alreadyShared: Boolean,
    enabled: Boolean,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = community.name, style = MaterialTheme.typography.titleSmall)
        if (alreadyShared) {
            Text(
                text = stringResource(R.string.pin_share_ya_compartido),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(onClick = onShare, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pin_share_boton))
            }
        }
    }
}
