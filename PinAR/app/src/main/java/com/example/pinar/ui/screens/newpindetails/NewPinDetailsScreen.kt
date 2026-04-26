package com.example.pinar.ui.screens.newpindetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.R
import com.example.pinar.ui.utils.InputCard
import com.example.pinar.ui.utils.TopBar

data class Categoria(
    val titulo: String,
    val icono: Int
)

val categoriasMock = listOf(
    Categoria( "Servicios", R.drawable.icono_servicios),
    Categoria("Alimentación", R.drawable.icono_alimentacion),
    Categoria("Comunidad", R.drawable.icono_comunidad)
)

@Composable
fun NewPinDetailsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onContinueClick: () -> Unit,
    viewModel: NewPinDetailsViewModel = viewModel()
){
    val state by viewModel.state
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(titulo = "Detalles", onBackClick = onBackClick, onCloseClick = onCloseClick)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { 0.666f },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Detalles del Pin",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Agrega información sobre tu nuevo Pin",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.imageplaceholder),
            contentDescription = "Imagen del pin",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(24.dp))
        InputCard(
            titulo = "Título",
            contador = "${state.titulo.length}/50"
        ) {
            OutlinedTextField(
                value = state.titulo,
                onValueChange = { viewModel.onTituloChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        Spacer(Modifier.height(24.dp))
        InputCard(
            titulo = "Descripción (Opcional)",
            contador = "${state.descripcion.length}/200"
        ) {
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.onDescripcionChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        Spacer(Modifier.height(24.dp))
        InputCard(
            titulo = "Categoría"
        ) {
            CategorySelector(
                categorias = categoriasMock,
                selected = state.categoriaSeleccionada,
                onSelect = { viewModel.onCategoriaSelect(it) }
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Continuar")
        }
    }

}

@Composable
fun CategorySelector(
    categorias: List<Categoria>,
    selected: Categoria?,
    onSelect: (Categoria) -> Unit
) {
    Column {
        categorias.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { categoria ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        onClick = { onSelect(categoria) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (categoria == selected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(categoria.icono),
                                contentDescription = categoria.titulo
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(categoria.titulo)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
