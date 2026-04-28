package com.example.pinar.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.data.UserData
import com.example.pinar.ui.theme.Charcoal
import com.example.pinar.ui.theme.RedPrimary
import com.example.pinar.ui.theme.SoftCream

@Composable
fun EditProfileScreen(
    userData: UserData?,
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val state by viewModel.state

    LaunchedEffect(userData) {
        viewModel.inicializar(userData)
    }

    Scaffold(
        topBar = { Cabecera(onBackClick) },
        containerColor = SoftCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SeccionAvatar(state.fotoUrl, state.nombre)
            
            SeccionCampos(
                nombre = state.nombre,
                biografia = state.biografia,
                onNombreChange = { viewModel.modificarNombre(it) },
                onBioChange = { viewModel.modificarBiografia(it) }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            BotonGuardar { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cabecera(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Editar Perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun SeccionAvatar(url: String, nombre: String) {
    Box(contentAlignment = Alignment.BottomEnd) {
        AsyncImage(
            model = url.ifEmpty { "https://ui-avatars.com/api/?name=$nombre&background=D32F2F&color=fff" },
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = RedPrimary
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@Composable
fun SeccionCampos(
    nombre: String,
    biografia: String,
    onNombreChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CampoEdicion(etiqueta = "Nombre", valor = nombre, onValueChange = onNombreChange)
        CampoEdicion(etiqueta = "Biografía", valor = biografia, onValueChange = onBioChange, esLineaUnica = false)
    }
}

@Composable
fun CampoEdicion(
    etiqueta: String,
    valor: String,
    onValueChange: (String) -> Unit,
    esLineaUnica: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal)
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = esLineaUnica,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
fun BotonGuardar(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
    ) {
        Text("Guardar", fontWeight = FontWeight.Bold)
    }
}
