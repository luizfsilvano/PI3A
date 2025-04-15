package com.example.routeshare.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMapScreen(
    sharedLatLng: LatLng = LatLng(-23.5505, -46.6333),
    onExit: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sharedLatLng, 16f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acompanhando rota") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = sharedLatLng),
                    title = "Localização compartilhada"
                )
                Circle(
                    center = sharedLatLng,
                    radius = 30.0, // em metros
                    fillColor = Color(0x330084d3),
                    strokeColor = Color(0xFF0084d3),
                    strokeWidth = 2f
                )
            }

            // Botões flutuantes
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Column {
                    IconButton(
                        onClick = {
                            cameraPositionState.move(CameraUpdateFactory.zoomIn())
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            cameraPositionState.move(CameraUpdateFactory.zoomOut())
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                IconButton(
                    onClick = {
                        // Reposiciona a câmera na localização compartilhada
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(sharedLatLng, 16f)
                        )
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = "Ir para localização", tint = Color.White)
                }
            }
        }
    }
}
