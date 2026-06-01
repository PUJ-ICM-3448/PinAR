package com.example.pinar.ui.screens.profile


import com.example.pinar.ui.utils.Footer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinar.R
import com.example.pinar.navigation.Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsActions.OnClick
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.data.UserData
import com.example.pinar.ui.screens.home.PinReciente
import com.google.maps.android.compose.CameraMoveStartedReason


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

    Box(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF7B1FA2),
                                    Color(0xFFD81B60)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Spacer(modifier = Modifier.height(20.dp))

                        FotoPerfil(userData)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userData?.nombre ?: stringResource(R.string.usuario),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Perfil creado el ${userData?.creacion?.toDate()}",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Text(
                            userData?.biografia ?: stringResource(R.string.el_usuario_no_tiene_biografia),
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))


                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            StatCard(state.lista.size.toString(),
                                stringResource(R.string.pines_creados)
                            )
                            StatCard(state.comentarios.toString(),
                                stringResource(R.string.comentarios_realizados)
                            )
                            //Cambiar cuando este terminado lo de comunidades
                            StatCard(stringResource(R.string._8), stringResource(R.string.logros))

                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEditProfile() },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.editar_perfil))
                            }
                        }

                    }
                }

            }

            item {

                Text(
                    stringResource(R.string.actividad_reciente),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

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
                    personas = it.visitas
                )
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = onClickLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                {
                    Text(stringResource(R.string.cerrar_sesi_n), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(150.dp))
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
fun FotoPerfil(userData: UserData?) {
    AsyncImage(
        model = userData?.fotoUrl,
        contentDescription = null,
        error = painterResource(R.drawable.profile),
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(50.dp))
    )
}

@Composable
fun StatCard(numero: String, texto: String) {

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                numero,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                texto,
                fontSize = 12.sp
            )

        }

    }

}
