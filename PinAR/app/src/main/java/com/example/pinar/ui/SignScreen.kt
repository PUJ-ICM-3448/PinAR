package com.example.pinar.ui
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.pinar.R
import com.example.pinar.ui.utils.LogoVertical
import com.example.pinar.ui.utils.PinArLogo

@Composable
fun SignButton(onClick: () -> Unit, text: String) {
    FilledTonalButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
fun Logos(modifier: Modifier = Modifier) {
    Row() {
        LogoVertical(icono = R.drawable.camera, text = stringResource(R.string.vista_ar))
        LogoVertical(icono = R.drawable.location, text = stringResource(R.string.pines))
        LogoVertical(icono = R.drawable.navigation, text = stringResource(R.string.navegaci_n))
    }
}

@Composable
fun SignScreen(modifier: Modifier = Modifier, onUserClick: () -> Unit = {}) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 60.dp)
            ) {
                PinArLogo()
                Text(
                    text = stringResource(R.string.pinar),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.realidad_aumentada_para_ubicarte_en_espacios_cerrados),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Logos(modifier = Modifier.padding(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                SignButton(onClick = onUserClick, text = stringResource(R.string.iniciar_sesi_n))
                SignButton(onClick = onUserClick, text = stringResource(R.string.crear_cuenta))
            }
        }
    }
}
