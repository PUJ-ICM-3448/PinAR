package com.example.pinar.ui.screens.map.util

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pinar.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberMarkerState

const val GOOGLE_MAP_CLOUD_ID = "24e6cc998b92b9e4c36bd06d"

@Composable
fun CustomMapMarker(
    imageUrl: Any?,
    fullName: String,
    location: LatLng,
    snippet: String? = null,
    markerColor: Color = MaterialTheme.colorScheme.primary,
    @DrawableRes placeholderResId: Int? = null,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(position = location)
    LaunchedEffect(location) {
        markerState.position = location
    }
    val shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)
    val hasRemoteImage = when (imageUrl) {
        null -> false
        is String -> imageUrl.isNotBlank()
        else -> true
    }
    val remotePainter = if (hasRemoteImage) {
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .allowHardware(false)
                .build()
        )
    } else {
        null
    }
    val placeholderPainter = if (!hasRemoteImage && placeholderResId != null) {
        painterResource(placeholderResId)
    } else {
        null
    }
    var expandMarker by remember { mutableStateOf(false) }

    MarkerComposable(
        keys = arrayOf<Any>(fullName, hasRemoteImage, placeholderResId ?: -1, expandMarker, markerColor),
        state = markerState,
        title = fullName,
        snippet = snippet,
        anchor = Offset(0.5f, 1f),
        onClick = {
            onClick()
            expandMarker = !expandMarker
            true
        }
    ) {
        Box(
            modifier = Modifier
                .size(if (expandMarker) 100.dp else 48.dp)
                .clip(shape)
                .background(markerColor)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                hasRemoteImage && remotePainter != null -> {
                    if (!expandMarker) {
                        Image(
                            painter = remotePainter,
                            contentDescription = fullName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = remotePainter,
                                contentDescription = fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = Color.White
                            )
                            if (snippet != null) {
                                Text(
                                    text = snippet,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                placeholderPainter != null -> {
                    if (!expandMarker) {
                        Image(
                            painter = placeholderPainter,
                            contentDescription = fullName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = placeholderPainter,
                                contentDescription = fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (snippet != null) {
                                Text(
                                    text = snippet,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                else -> {
                    if (!expandMarker) {
                        Text(
                            text = fullName.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fullName.take(1).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (snippet != null) {
                                Text(
                                    text = snippet,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinMapMarker(
    title: String,
    snippet: String?,
    location: LatLng,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(position = location)
    LaunchedEffect(location) {
        markerState.position = location
    }
    var expandMarker by remember { mutableStateOf(false) }
    val pinPainter = painterResource(R.drawable.foto_pin)

    MarkerComposable(
        keys = arrayOf<Any>(title, expandMarker),
        state = markerState,
        title = title,
        snippet = snippet,
        anchor = Offset(0.5f, 1f),
        onClick = {
            onClick()
            expandMarker = !expandMarker
            true
        }
    ) {
        if (!expandMarker) {
            Image(
                painter = pinPainter,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.size(width = 120.dp, height = 100.dp)
            ) {
                Image(
                    painter = pinPainter,
                    contentDescription = title,
                    modifier = Modifier
                        .size(56.dp)
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    maxLines = 2
                )
                if (snippet != null) {
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
