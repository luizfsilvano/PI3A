package com.example.routeshare.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.routeshare.MainActivity // Importe sua Activity principal
import com.example.routeshare.R // Importe a classe R do seu projeto
import com.google.android.gms.location.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class LocationSharingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var webSocket: WebSocket? = null

    // Configura o client do OkHttp para ter um timeout longo, essencial para WebSockets
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "LocationSharingChannel"
        const val NOTIFICATION_ID = 12345 // Um ID único para a notificação
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val shareId = intent.getStringExtra("SHARE_ID") ?: return START_NOT_STICKY
                startForeground(NOTIFICATION_ID, createNotification(shareId))
                connectWebSocket(shareId)
                startLocationUpdates()
            }
            "STOP" -> {
                stopSelf() // Para o serviço
            }
        }
        return START_STICKY
    }

    private fun connectWebSocket(shareId: String) {
        // ATENÇÃO: Use o IP correto do seu servidor FastAPI!
        // 10.0.2.2 é para o emulador Android se conectar ao localhost da sua máquina.
        // Se usar um celular físico, ambos devem estar na mesma rede Wi-Fi,
        // e você deve usar o IP da sua máquina na rede (ex: 192.168.1.10).
        val request = Request.Builder().url("ws://10.0.2.2:8000/ws/share/$shareId").build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("COMPARTILHADOR: Conectado ao servidor com ID: $shareId")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("COMPARTILHADOR: Falha na conexão WebSocket: ${t.message}")
                // Você pode adicionar uma lógica para tentar reconectar aqui.
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L) // A cada 5 segundos
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val payload = """{"lat":${location.latitude},"lng":${location.longitude}}"""
                    val sent = webSocket?.send(payload)
                    if (sent == true) {
                        println("Localização enviada: $payload")
                    } else {
                        println("Falha ao enviar localização: WebSocket não está pronto.")
                    }
                }
            }
        }
        // A permissão é checada na UI antes de iniciar o serviço
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun createNotification(shareId: String): Notification {
        createNotificationChannel()

        // Intent para parar o serviço quando o botão na notificação for clicado
        val stopIntent = Intent(this, LocationSharingService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Intent para abrir o app quando a notificação for clicada
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(this, 1, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Route Share está ativo")
            .setContentText("Sua localização está sendo compartilhada.")
            .setSmallIcon(R.drawable.ic_notification_icon) // SUBSTITUA pelo seu ícone
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_stop_icon, "Parar", stopPendingIntent) // SUBSTITUA pelo seu ícone
            .setOngoing(true) // Torna a notificação não-removível por deslize
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal de Compartilhamento de Localização",
                NotificationManager.IMPORTANCE_LOW // Use LOW para evitar som a cada atualização
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        webSocket?.close(1000, "Serviço parado pelo usuário.")
        println("Serviço de localização parado e WebSocket fechado.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}