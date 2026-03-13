package com.example.pinar.ui.utils

import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinar.R
import com.example.pinar.ui.theme.RedDeep
import com.example.pinar.ui.theme.RedPrimary
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.sp

@Composable
fun PinMiniLogo() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = RedPrimary,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.location),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
fun PinArLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pinarlogo),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun IconoConTexto(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color.Gray
        )
    }
}

@Composable
fun LogoVertical(modifier: Modifier = Modifier, icono: Int, text: String) {
    Box() {
        Column(modifier = Modifier) {
            Image(
                painter = painterResource(icono),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
            )
        }
    }
}

@Composable
fun Footer(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Inicio", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.map),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Mapa", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.clock),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("AR", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Perfil", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.Transparent
            )
        )
    }
}