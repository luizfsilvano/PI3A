package com.example.routeshare.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.routeshare.services.LocationSharingService
import kotlinx.coroutines.launch
import com.google.maps.android.PolyUtil
import java.util.UUID
import android.content.Context

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val rotaCodificada = "bvs_Bt_mcHmAuBcBnAia@wo@_A@eBtAAdAyAlAy@h@}AhA" // Exemplo curto
    val pontosDaRotaReal: List<LatLng> = PolyUtil.decode(rotaCodificada)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val scope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    // --- Lógica de Permissões ---
    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    // Solicita a permissão principal ao entrar na tela
    LaunchedEffect(Unit) {
        fineLocationPermission.launchPermissionRequest()
    }

    val hasLocationPermission = fineLocationPermission.status.isGranted

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-23.5505, -46.6333), 14f)
    }

    // Efeito para buscar a localização atual quando a permissão for concedida
    if (hasLocationPermission) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LaunchedEffect(key1 = fusedLocationClient) {
            // Tenta obter a última localização conhecida (é rápido)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    currentLocation = latLng // Atualiza nosso estado
                    // Move a câmera para a localização inicial do usuário
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Route Share", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        if (isSharing) {
                            isSharing = false
                            val serviceIntent = Intent(context, LocationSharingService::class.java).apply { action = "STOP" }
                            context.startService(serviceIntent)
                        } else {

                            if (!fineLocationPermission.status.isGranted) {
                                fineLocationPermission.launchPermissionRequest()
                            }
                            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationPermission.status.isGranted) {
                                backgroundLocationPermission.launchPermissionRequest()
                            }
                            else {
                                isSharing = true
                                startSharingProcess(context)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSharing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    // --- CONTEÚDO DO BOTÃO DINÂMICO ---
                    val icon = if (isSharing) Icons.Default.Stop else Icons.Default.Share
                    Icon(icon, contentDescription = "Compartilhar / Parar")

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(if (isSharing) "Parar de Compartilhar" else "Compartilhar")
                }
            }
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
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                Polyline(
                    points = pontosDaRotaReal,
                    color = Color(0xFF0084d3), // Uma cor azul bonita
                    width = 20f,               // Largura da linha no mapa
                    startCap = RoundCap(),     // Deixa o início da linha redondo
                    endCap = RoundCap()        // Deixa o final da linha redondo
                )
                if (pontosDaRotaReal.isNotEmpty()) {
                    Marker(
                        state = MarkerState(position = pontosDaRotaReal.last()),
                        title = "Fim da Rota"
                    )
                }
            }

            // Campo de busca
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = {
                            Text("Search or enter address", color = Color.White)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
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
                        currentLocation?.let { location ->
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = "Navigation", tint = Color.White)
                }
            }
        }
    }
}

private fun startSharingProcess(context: Context) {
    // Para simplificar, usamos um ID aleatório. Num app real, viria do backend.
    val shareId = UUID.randomUUID().toString()

    // Inicia o serviço em segundo plano
    val serviceIntent = Intent(context, LocationSharingService::class.java).apply {
        action = "START"
        putExtra("SHARE_ID", shareId)
    }
    ContextCompat.startForegroundService(context, serviceIntent)

    // Cria e mostra o Share Sheet
    val shareLink = "https://routeshare.example.com/view/$shareId"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Acompanhe minha rota em tempo real: $shareLink")
    }
    // Adiciona a flag para poder iniciar a activity de um contexto que não é uma activity (como o de um Composable)
    val chooser = Intent.createChooser(intent, "Compartilhar via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}