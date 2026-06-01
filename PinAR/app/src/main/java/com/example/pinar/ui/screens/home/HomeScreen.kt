package com.example.pinar.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.data.UserData
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.utils.IconoConTexto
import com.example.pinar.ui.utils.PinMiniLogo
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Home,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPins: () -> Unit = {},
    onNavigateToPinDetail: (String) -> Unit = {},
    userData: UserData?,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 160.dp)
        ) {

            // --- Saludo ---
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.hola2, userData?.nombre ?: "Usuario"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.explora_y_navega_por_espacios_interiores),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Cards de acción rápida ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CardAccion(
                        modifier = Modifier.weight(1f).clickable { onNavigateToAR() },
                        texto = stringResource(R.string.crear_pin),
                        textoMini = stringResource(R.string.vista_ar),
                        iconRes = R.drawable.location
                    )
                    CardAccion(
                        modifier = Modifier.weight(1f).clickable { onNavigateToMap() },
                        texto = stringResource(R.string.ver_mapa),
                        textoMini = stringResource(R.string.navegaci_n),
                        iconRes = R.drawable.map
                    )
                }
            }

            // --- Sección: Pines Recientes ---
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SeccionHeader(
                    titulo = stringResource(R.string.pines_recientes),
                    accion = stringResource(R.string.ver_todos),
                    onAccionClick = onNavigateToPins
                )
            }

            items(state.lista.sortedByDescending { it.fecha }.take(3)) { pin ->
                val fechaLegible = pin.fecha?.toDate()?.let { d ->
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(d)
                } ?: "Reciente"

                PinReciente(
                    nombre = pin.title,
                    sitio = pin.description,
                    tiempo = fechaLegible,
                    personas = pin.visitas,
                    onClick = { onNavigateToPinDetail(pin.id) }
                )
            }

            // --- Sección: Lugares Populares ---
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SeccionHeader(titulo = stringResource(R.string.lugares_populares))
            }

            items(state.lista.sortedByDescending { it.visitas }.take(3)) { pin ->
                Trending(
                    modifier = Modifier.fillMaxWidth(),
                    sitio = pin.title,
                    visitas = pin.visitas.toString(),
                    onClick = { onNavigateToPinDetail(pin.id) }
                )
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

// --- Componentes ---

@Composable
fun SeccionHeader(titulo: String, accion: String? = null, onAccionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (accion != null) {
            Text(
                text = accion,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAccionClick() }
            )
        }
    }
}

@Composable
fun CardAccion(
    modifier: Modifier = Modifier,
    texto: String,
    textoMini: String,
    iconRes: Int
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = textoMini,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PinReciente(
    modifier: Modifier = Modifier,
    nombre: String,
    sitio: String,
    tiempo: String,
    personas: Int,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                PinMiniLogo()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sitio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoConTexto(R.drawable.clock, stringResource(R.string.hace, tiempo))
                    Spacer(modifier = Modifier.width(12.dp))
                    IconoConTexto(R.drawable.user, personas.toString())
                }
            }
        }
    }
}

@Composable
fun Trending(
    modifier: Modifier = Modifier,
    sitio: String,
    visitas: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = sitio,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.visitas, visitas),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
