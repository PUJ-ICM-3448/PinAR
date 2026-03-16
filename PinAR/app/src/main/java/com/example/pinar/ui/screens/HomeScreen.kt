package com.example.pinar.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.pinar.ui.utils.Footer
import com.example.pinar.ui.utils.PinArLogo
import androidx.compose.material3.CardDefaults

@Composable
fun CardPin(modifier: Modifier = Modifier, texto: String, textoMini: String) {
    Card(
        modifier = modifier,
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
fun PinDetallesInferiores(tiempo: String, distancia: String, personas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoConTexto(
            R.drawable.clock,
            stringResource(R.string.hace, tiempo)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = distancia,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconoConTexto(
            R.drawable.user,
            personas.toString()
        )
    }
}


@Composable
fun PinReciente(modifier: Modifier = Modifier, nombre: String, sitio: String, tiempo: String, distancia: String, personas: Int) {
    Card(
        modifier = modifier,
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
                PinDetallesInferiores(tiempo, distancia, personas)
            }
        }
    }
}

@Composable
fun Trending(modifier: Modifier = Modifier, sitio: String, visitas: String) {
    Card(
        modifier = modifier,
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

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    PinArLogo(modifier = Modifier.size(170.dp))
                    //Despues se cambia para que reciba el texto el nombre de usuario
                    Text(
                        stringResource(R.string.hola_usuario),
                        fontWeight = FontWeight.Bold
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
                        textoMini = stringResource(R.string.vista_ar)
                    )
                    CardPin(
                        modifier = Modifier.weight(1f),
                        texto = stringResource(R.string.ver_mapa),
                        textoMini = stringResource(R.string.navegaci_n)
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
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            item {
                //Tambien cambiar esto en el futuro para que sea variable
                PinReciente(
                    nombre = stringResource(R.string.sala_de_conferencias_a),
                    sitio = stringResource(R.string.edificio_principal_piso_3),
                    tiempo = "5 min",
                    distancia = "12m",
                    personas = 3
                )
            }
            item {
                PinReciente(
                    nombre = stringResource(R.string.cafeter_a),
                    sitio = stringResource(R.string.edificio_principal_piso_1),
                    tiempo = "15 min",
                    distancia = "45m",
                    personas = 8
                )
            }
            item {
                PinReciente(
                    nombre = stringResource(R.string.laboratorio_204),
                    sitio = stringResource(R.string.edificio_de_investigaci_n_piso_2),
                    tiempo = "1 hora",
                    distancia = "120m",
                    personas = 5
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

            //Y estos tambien
            item {
                Trending(
                    modifier = Modifier.fillMaxWidth(),
                    sitio = stringResource(R.string.auditorio_principal),
                    visitas = "234"
                )
            }
            item {
                Trending(
                    modifier = Modifier.fillMaxWidth(),
                    sitio = stringResource(R.string.biblioteca),
                    visitas = "189"
                )
            }
            item {
                Trending(
                    modifier = Modifier.fillMaxWidth(),
                    sitio = "Gimnasio",
                    visitas = "156"
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }

        }
        Footer(
            modifier = Modifier.align(Alignment.BottomCenter),
            onMapClick = onNavigateToMap,
            onProfileClick = onNavigateToProfile
        )
    }

}
