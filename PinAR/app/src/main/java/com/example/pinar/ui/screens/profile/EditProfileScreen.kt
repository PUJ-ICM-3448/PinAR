package com.example.pinar.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.R
import com.example.pinar.data.UserData
import com.example.pinar.ui.MainViewModel
import com.example.pinar.ui.theme.Charcoal
import com.example.pinar.ui.theme.RedPrimary

@Composable
fun EditProfileScreen(
    userData: UserData?,
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state
    val context = LocalContext.current
    val onePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.seleccionarFoto(it, context) }
    }

    LaunchedEffect(userData) {
        viewModel.inicializar(userData)
    }

    Scaffold(
        topBar = { Cabecera(onBackClick) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SeccionAvatar(
                url = state.fotoUrl,
                uri = state.fotoUri,
                nombre = state.nombre,
                onImageClick = { onePhotoPickerLauncher.launch("image/*") }
            )
            
            SeccionCampos(
                nombre = state.nombre,
                biografia = state.biografia,
                onNombreChange = { viewModel.modificarNombre(it) },
                onBioChange = { viewModel.modificarBiografia(it) },
                onImageClick = { onePhotoPickerLauncher.launch("image/*") },
                state = state
            )

            Spacer(modifier = Modifier.weight(1f))
            
            BotonGuardar {
                mainViewModel.modificarDatos(
                    state.nombre,
                    state.biografia,
                    state.uid.ifBlank { userData?.uid.orEmpty() },
                    state.fotoUri,
                    context
                )
                onBackClick()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cabecera(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.editar_perfilcab), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun SeccionAvatar(url: String, uri: Uri?, nombre: String, onImageClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onImageClick() }
    ) {
        AsyncImage(
            model = uri ?: url.ifEmpty { "https://ui-avatars.com/api/?name=$nombre&background=D32F2F&color=fff" },
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
    onBioChange: (String) -> Unit,
    onImageClick: () -> Unit,
    state: EditProfileState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CampoEdicion(
            etiqueta = stringResource(R.string.nombre),
            valor = nombre, 
            onValueChange = onNombreChange
        )
        
        CampoEdicion(
            etiqueta = stringResource(R.string.biograf_a),
            valor = biografia, 
            onValueChange = onBioChange, 
            esLineaUnica = false
        )

        OutlinedButton(
            onClick = { onImageClick() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Charcoal)
        ) {
            Text(text = stringResource(R.string.seleccionar_foto_de_perfil))
        }
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
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = esLineaUnica,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun BotonGuardar(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
    ) {
        Text(stringResource(R.string.guardar), fontWeight = FontWeight.Bold)
    }
}
