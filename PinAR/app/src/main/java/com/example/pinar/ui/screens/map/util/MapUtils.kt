package com.example.pinar.ui.screens.map.util

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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun CustomMapMarker(
    imageUrl: Any?,
    fullName: String,
    location: LatLng,
    snippet: String? = null,
    markerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
){
    val markerState = rememberMarkerState(position = location)
    LaunchedEffect(location) {
        markerState.position = location
    }
    val shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .allowHardware(false)
            .build()
    )
    var expandMarker by remember { mutableStateOf(false) }

    MarkerComposable(
        keys = arrayOf(fullName, painter.state, expandMarker, markerColor),
        state = markerState,
        title = fullName,
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
        ){
            if(imageUrl != null){
                if(!expandMarker){
                    Image(
                        painter = painter,
                        contentDescription = "Place info image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }else{
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Place info image",
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
            }else{
                if(!expandMarker){
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