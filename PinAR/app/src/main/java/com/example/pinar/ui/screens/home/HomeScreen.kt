package com.example.pinar.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinar.R
import com.example.pinar.ui.utils.PinMiniLogo
import com.example.pinar.ui.utils.IconoConTexto
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.pinar.ui.utils.Footer
import com.example.pinar.navigation.Screen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.data.UserData
import androidx.compose.foundation.lazy.items

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

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(
                top = 40.dp,
                bottom = 160.dp
            )
        ) {
            item {
                Column(
                    modifier = Modifier.padding(top = 40.dp),
                ) {
                    Text(
                        text = stringResource(R.string.hola2, userData?.nombre ?: "Usuario"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                    Text(
                        stringResource(R.string.explora_y_navega_por_espacios_interiores),
                        fontSize = 20.sp
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardPin(
                        modifier = Modifier.weight(1f),
                        texto = stringResource(R.string.crear_pin),
                        textoMini = stringResource(R.string.vista_ar),
                        onNavigate = { onNavigateToAR() }
                    )
                    CardPin(
                        modifier = Modifier.weight(1f),
                        texto = stringResource(R.string.ver_mapa),
                        textoMini = stringResource(R.string.navegaci_n),
                        onNavigate = { onNavigateToMap() }
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.pines_recientes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = stringResource(R.string.ver_todos),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToPins() }
                    )
                }
            }

            items (
                state.lista.sortedByDescending { it.fecha }.take(3)
            ) {
                val fechaLegible = it.fecha?.toDate()?.let { d ->
                    java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(d)
                } ?: "Reciente"

                PinReciente(
                    nombre = it.title,
                    sitio = it.description,
                    tiempo = fechaLegible,
                    personas = it.visitas,
                    onClick = { onNavigateToPinDetail(it.id) }
                )
            }
            item {
                Text(
                    text = stringResource(R.string.lugares_populares),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items (
                state.lista.sortedByDescending { it.visitas }.take(3)
            ) {
                Trending(
                    modifier = Modifier.fillMaxWidth(),
                    sitio = it.title,
                    visitas = it.visitas.toString(),
                    onClick = { onNavigateToPinDetail(it.id) }
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

@Composable
fun CardPin(modifier: Modifier = Modifier, texto: String, textoMini: String, onNavigate: () -> Unit) {
    Card(
        modifier = modifier.clickable { onNavigate() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {
            PinMiniLogo()
            Text(texto)
            Text(
                textoMini,
                color= MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp
            )
        }
    }

}

@Composable
fun PinInfoPrincipal(titulo: String, subtitulo: String) {
    Column {
        Text(
            titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PinDetallesInferiores(tiempo: String, personas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoConTexto(
            R.drawable.clock,
            stringResource(R.string.hace, tiempo)
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconoConTexto(
            R.drawable.user,
            personas.toString()
        )
    }
}


@Composable
fun PinReciente(modifier: Modifier = Modifier, nombre: String, sitio: String, tiempo: String, personas: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinMiniLogo()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                PinInfoPrincipal(titulo = nombre, subtitulo = sitio)
                Spacer(modifier = Modifier.height(8.dp))
                PinDetallesInferiores(tiempo, personas)
            }
        }
    }
}

@Composable
fun Trending(modifier: Modifier = Modifier, sitio: String, visitas: String, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(R.drawable.trend),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(sitio)
            }
            Text(stringResource(R.string.visitas, visitas))
        }
    }
}
