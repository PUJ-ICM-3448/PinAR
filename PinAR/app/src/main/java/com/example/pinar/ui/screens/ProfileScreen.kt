package com.example.pinar.ui.screens


import com.example.pinar.ui.utils.Footer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Profile,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {

    Box(modifier = modifier.fillMaxSize()) {

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

                        Image(
                            painter = painterResource(R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "María González",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Entusiasta de la tecnología AR y exploradora de espacios interiores.",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))


                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            StatCard("42", "Pines\nCreados")
                            StatCard("128", "Rutas\nNavegadas")
                            StatCard("8", "Logros")

                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Editar Perfil")
                            }
                        }

                    }
                }

            }

            item {

                Text(
                    "Actividad Reciente",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

            }

            item {
                PinReciente(
                    nombre = "Sala A",
                    sitio = "Edificio 1",
                    tiempo = "5 min",
                    distancia = "12m",
                    personas = 3
                )
            }

            item {
                PinReciente(
                    nombre = "Biblioteca",
                    sitio = "Piso 2",
                    tiempo = "10 min",
                    distancia = "30m",
                    personas = 5
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }

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