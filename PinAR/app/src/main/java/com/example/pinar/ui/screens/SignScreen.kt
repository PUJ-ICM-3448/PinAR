package com.example.pinar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinar.R
import com.example.pinar.ui.utils.LogoVertical
import com.example.pinar.ui.utils.PinArLogo

@Composable
fun SignButton(onClick: () -> Unit, text: String) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun Logos(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
    ) {
        LogoVertical(icono = R.drawable.camera, text = stringResource(R.string.vista_ar))
        LogoVertical(icono = R.drawable.location, text = stringResource(R.string.pines))
        LogoVertical(icono = R.drawable.navegaci_on, text = stringResource(R.string.navegaci_n))
    }
}

@Composable
fun SignScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            PinArLogo()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.pinar),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.realidad_aumentada_para_ubicarte_en_espacios_cerrados),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Logos(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(48.dp))
            SignButton(
                onClick = onNavigateToLogin,
                text = stringResource(R.string.iniciar_sesi_n)
            )
            Spacer(modifier = Modifier.height(16.dp))
            SignButton(
                onClick = onNavigateToRegister,
                text = stringResource(R.string.crear_cuenta)
            )
        }
    }
}