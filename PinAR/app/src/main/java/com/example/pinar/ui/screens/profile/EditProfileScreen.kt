package com.example.pinar.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinar.data.UserData
import com.example.pinar.ui.theme.Charcoal
import com.example.pinar.ui.theme.RedDark
import com.example.pinar.ui.theme.RedPrimary

@Composable
fun EditProfileScreen(userData: UserData?, modifier: Modifier = Modifier, onProfileClick: () -> Unit, ) {
    Column(modifier = modifier) {
        Text(
            text = "Editar Perfil",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Nombre",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Charcoal
            )
        }
    }
}
