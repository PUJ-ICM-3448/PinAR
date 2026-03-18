package com.example.pinar.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinar.R
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.theme.MutedGold
import com.example.pinar.ui.theme.SoftCream
import com.example.pinar.ui.utils.Footer
import com.example.pinar.data.models.Notification
import com.example.pinar.data.models.NotificationType
import com.example.pinar.data.mock.mockNotifications
import com.example.pinar.ui.utils.avatarColorFor
import com.example.pinar.ui.utils.initialFromDetalle


@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val notificaciones = remember { mutableStateListOf(*mockNotifications.toTypedArray()) }
    var todasLeidas by remember { mutableStateOf(false) }
    var filtroSeleccionado by remember { mutableStateOf(NotificationType.ALL) }
    var filterChipsVisible by remember { mutableStateOf(false) }

    val unreadCount = notificaciones.count { it.unread && !todasLeidas }

    // Lista filtrada según el chip seleccionado
    val notificacionesFiltradas = if (filtroSeleccionado == NotificationType.ALL) notificaciones
    else notificaciones.filter { it.type == filtroSeleccionado }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 30.dp)
        ) {
            // Encabezado: icono campana, título, subtítulo sin leer, icono filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.notificaciones),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = stringResource(R.string.sin_leer, unreadCount),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                IconButton(onClick = { filterChipsVisible = !filterChipsVisible }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (filterChipsVisible) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chips de filtro: solo visibles al presionar el icono de filtro
            AnimatedVisibility(
                visible = filterChipsVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FilterChipsRow(
                    filtroSeleccionado = filtroSeleccionado,
                    onFiltroSelected = { filtroSeleccionado = it }
                )
            }

            // Barra de acciones: marcar todas leídas, papelera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { todasLeidas = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.marcar_todas_leidas),
                        fontSize = 14.sp
                    )
                }
                IconButton(
                    onClick = { notificaciones.clear() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Lista de notificaciones o estado vacío
            if (notificacionesFiltradas.isEmpty()) {
                EmptyNotificationsState(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(notificacionesFiltradas) { _, notif ->
                        NotificationCard(
                            titulo = notif.title,
                            detalle = notif.detail,
                            preview = notif.preview,
                            tiempo = notif.time,
                            unread = notif.unread && !todasLeidas,
                            onDismiss = { notificaciones.remove(notif) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        Footer(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentScreen = Screen.Notifications,
            unreadCount = unreadCount,
            onHomeClick = onNavigateToHome,
            onMapClick = onNavigateToMap,
            onARClick = onNavigateToAR,
            onNotificationsClick = { },
            onProfileClick = onNavigateToProfile
        )
    }
}


// Tarjeta de notificación: avatar circular con inicial y color aleatorio, título, detalle, preview chip, tiempo y cerrar
@Composable
private fun NotificationCard(
    modifier: Modifier = Modifier,
    titulo: String,
    detalle: String,
    preview: String?,
    tiempo: String,
    unread: Boolean,
    onDismiss: () -> Unit
) {
    val avatarBg = avatarColorFor(detalle)
    val initial = initialFromDetalle(detalle)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar circular con inicial y fondo de color según el nombre
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Fila: título + punto sin leer a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (unread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (preview != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Preview en un chip redondeado
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Fila inferior: tiempo a la izquierda, cerrar a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tiempo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Fila de chips de filtro con estilo mejorado: seleccionado azul sólido, no seleccionado borde gris
@Composable
private fun FilterChipsRow(
    modifier: Modifier = Modifier,
    filtroSeleccionado: NotificationType,
    onFiltroSelected: (NotificationType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(NotificationType.ALL, NotificationType.COMMENTS, NotificationType.LIKES).forEach { tipo ->
                FilterChipItem(
                    tipo = tipo,
                    selected = filtroSeleccionado == tipo,
                    onFiltroSelected = onFiltroSelected
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(NotificationType.FOLLOWERS, NotificationType.COMMUNITIES, NotificationType.SYSTEM).forEach { tipo ->
                FilterChipItem(
                    tipo = tipo,
                    selected = filtroSeleccionado == tipo,
                    onFiltroSelected = onFiltroSelected
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    tipo: NotificationType,
    selected: Boolean,
    onFiltroSelected: (NotificationType) -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { onFiltroSelected(tipo) },
        label = {
            Text(
                text = when (tipo) {
                    NotificationType.ALL -> stringResource(R.string.filtro_todas)
                    NotificationType.COMMENTS -> stringResource(R.string.filtro_comentarios)
                    NotificationType.LIKES -> stringResource(R.string.filtro_me_gusta)
                    NotificationType.FOLLOWERS -> stringResource(R.string.filtro_seguidores)
                    NotificationType.COMMUNITIES -> stringResource(R.string.filtro_comunidades)
                    NotificationType.SYSTEM -> stringResource(R.string.filtro_sistema)
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (tipo) {
                    NotificationType.ALL -> Icons.Outlined.Notifications
                    NotificationType.COMMENTS -> Icons.Outlined.ChatBubbleOutline
                    NotificationType.LIKES -> Icons.Outlined.FavoriteBorder
                    NotificationType.FOLLOWERS -> Icons.Outlined.Person
                    NotificationType.COMMUNITIES -> Icons.Outlined.LocationOn
                    NotificationType.SYSTEM -> Icons.Outlined.Settings
                },
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
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

// Estado vacío: icono de campana grande, título y subtítulo
@Composable
private fun EmptyNotificationsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_hay_notificaciones),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.notificaciones_apareceran_aqui),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
