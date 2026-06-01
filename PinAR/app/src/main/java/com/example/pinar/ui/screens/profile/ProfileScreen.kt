package com.example.pinar.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.R
import com.example.pinar.data.UserData
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.screens.home.MetaChip
import com.example.pinar.ui.screens.home.PinReciente
import com.example.pinar.ui.utils.Footer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Profile,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    userData: UserData?,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state

    LaunchedEffect(Unit) {
        viewModel.inicializar(userData)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // --- Header de perfil ---
            item {
                ProfileHeader(
                    userData = userData,
                    pinCount = state.lista.size,
                    comentariosCount = state.comentarios,
                    onEditClick = onNavigateToEditProfile
                )
            }

            // --- Actividad reciente ---
            item {
                Text(
                    text = stringResource(R.string.actividad_reciente),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            if (state.lista.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin actividad reciente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.lista.sortedByDescending { it.fecha }.take(3)) { pin ->
                    val fechaLegible = pin.fecha?.toDate()?.let { d ->
                        SimpleDateFormat("dd MMM", Locale.getDefault()).format(d)
                    } ?: "Reciente"

                    PinReciente(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        nombre = pin.title,
                        sitio = pin.description,
                        tiempo = fechaLegible,
                        personas = pin.visitas
                    )
                }
            }

            // --- Botón cerrar sesión ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onClickLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.cerrar_sesi_n),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Footer(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentScreen = currentScreen,
            unreadCount = 3,
            onHomeClick = onNavigateToHome,
            onMapClick = onNavigateToMap,
            onARClick = onNavigateToAR,
            onNotificationsClick = onNavigateToNotifications,
            onProfileClick = onNavigateToProfile
        )
    }
}

@Composable
private fun ProfileHeader(
    userData: UserData?,
    pinCount: Int,
    comentariosCount: Int,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de perfil con marco
        Box(
            modifier = Modifier
                .size(94.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = userData?.fotoUrl,
                contentDescription = null,
                error = painterResource(R.drawable.profile),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Nombre
        Text(
            text = userData?.nombre ?: stringResource(R.string.usuario),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fecha de creación
        val fechaCreacion = userData?.creacion?.toDate()?.let { d ->
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d)
        }
        if (fechaCreacion != null) {
            Text(
                text = "Miembro desde $fechaCreacion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Biografía
        val bio = userData?.biografia?.takeIf { it.isNotBlank() }
        Text(
            text = bio ?: stringResource(R.string.el_usuario_no_tiene_biografia),
            style = MaterialTheme.typography.bodyMedium,
            color = if (bio != null)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats — Uso de IntrinsicSize.Min para que todas tengan la misma altura
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                numero = pinCount.toString(),
                texto = stringResource(R.string.pines_creados)
            )
            StatCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                numero = comentariosCount.toString(),
                texto = stringResource(R.string.comentarios_realizados)
            )
            StatCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                numero = stringResource(R.string._8),
                texto = stringResource(R.string.logros)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón editar perfil
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = stringResource(R.string.editar_perfil),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    numero: String,
    texto: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
