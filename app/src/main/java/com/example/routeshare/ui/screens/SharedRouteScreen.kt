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
import androidx.core.content.ContextCompat
import com.example.routeshare.R
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.awaitCancellation
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMapScreen(
    shareId: String,
    onExit: () -> Unit
) {
    val rotaCodificada = "bvs_Bt_mcHmAuBcBnAia@wo@_A@eBtAAdAyAlAy@h@}AhA"
    val context = LocalContext.current
    val pontosDaRotaReal: List<LatLng> = PolyUtil.decode(rotaCodificada)
    var sharedLatLng by remember { mutableStateOf(LatLng(-23.5505, -46.6333)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sharedLatLng, 16f)
    }

    LaunchedEffect(key1 = shareId) {
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://10.0.2.2:8000/ws/share/$shareId").build()
        var ws: WebSocket? = null

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Nova localização recebida! Atualiza o estado
                try {
                    val json = JSONObject(text)
                    sharedLatLng = LatLng(json.getDouble("lat"), json.getDouble("lng"))
                } catch (e: Exception) {
                    println("Erro ao parsear JSON da localização: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Conexão WebSocket falhou: ${t.message}")
            }
        }

        try {
            // Inicia a conexão
            ws = client.newWebSocket(request, listener)
            // Mantém a coroutine suspensa até ser cancelada (quando o Composable sai da tela)
            awaitCancellation()
        } finally {
            // Este bloco é executado quando a coroutine é cancelada
            ws?.close(1000, "Saindo da tela")
            println("Conexão WebSocket fechada.")
        }
    }

    // Anima a câmera para a nova posição
    LaunchedEffect(key1 = sharedLatLng) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(sharedLatLng))
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
                Polyline(
                    points = pontosDaRotaReal,
                    color = Color(0xFF0084d3), // Uma cor azul bonita
                    width = 20f,               // Largura da linha no mapa
                    startCap = RoundCap(),     // Deixa o início da linha redondo
                    endCap = RoundCap()        // Deixa o final da linha redondo
                )
                // Aqui você pode adicionar marcadores para o início e fim da rota, se quiser
                if (pontosDaRotaReal.isNotEmpty()) {
                    Marker(
                        state = MarkerState(position = pontosDaRotaReal.last()),
                        title = "Fim da Rota",
                        icon = bitmapDescriptorFromVector(context, R.drawable.ic_finish_flag)
                    )
                }

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

// Adicione esta função no final do seu arquivo
private fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {
    // Adicione os imports necessários com Alt+Enter
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = android.graphics.Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}