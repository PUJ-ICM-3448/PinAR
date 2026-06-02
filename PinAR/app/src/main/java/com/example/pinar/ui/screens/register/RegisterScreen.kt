package com.example.pinar.ui.screens.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pinar.R
import com.example.pinar.data.AuthState

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onClickRegister: (String, String, String, String, Uri?, android.content.Context) -> Unit = { _, _, _, _, _, _ -> },
    authState: AuthState = AuthState.noAutenticado,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val onePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.seleccionarFoto(it, context) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // --- TopBar manual ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- Encabezado ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.crear_cuenta),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.completa_tus_datos_para_registrarte),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // --- Campos ---
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // Avatar Picker
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onePhotoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.fotoUri != null) {
                                AsyncImage(
                                    model = state.fotoUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Badge pequeño para indicar acción
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Nombre
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.nombre_completo),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = state.nombre,
                            onValueChange = { viewModel.onNombreChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.tu_nombre)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Correo
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.correo_electr_nico),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.tu_email_com)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Biografía
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.biograf_a),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = state.biografia,
                            onValueChange = { viewModel.onBiografiaChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.cu_ntanos_sobre_ti)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Contraseña
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.contrase_a),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Confirmar Contraseña
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.confirmar_contrase_a),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onConfirmPasswordChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Error
                    if (authState is AuthState.Error) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authState.mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            // --- Acciones inferiores ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Button(
                    onClick = {
                        onClickRegister(
                            state.nombre,
                            state.email,
                            state.password,
                            state.biografia,
                            state.fotoUri,
                            context
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.registrarse),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ya_tienes_cuenta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.inicia_sesi_n),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
