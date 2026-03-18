package com.example.pinar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.pinar.ui.utils.TopBar;

@Composable
fun NewPinScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onTakePhoto: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(titulo = "Nuevo pin", onBackClick = onBackClick, onCloseClick = onCloseClick)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { 0.333f },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Agrega una foto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Captura o selecciona una imagen para tu publicación",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))
        CardZonaImagen()
        Spacer(Modifier.height(24.dp))
        BotonTomarFoto(onTakePhoto)
        Spacer(Modifier.height(12.dp))
        BotonGaleria()
    }
}


@Composable
fun CardZonaImagen(){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text("No hay foto seleccionada")
        }
    }
}

@Composable
fun BotonTomarFoto(
    onTakePhoto: () -> Unit
){
    Button(
        onClick = onTakePhoto,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        Icon(Icons.Default.PhotoCamera, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Tomar foto")
    }
}

@Composable
fun BotonGaleria(){
    OutlinedButton(
        onClick ={ },
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        Icon(Icons.Default.Image, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Seleccionar imagen de galería")
    }
}