package com.example.routeshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.routeshare.ui.screens.MapScreen
import com.example.routeshare.ui.theme.RouteShareTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.routeshare.ui.screens.SharedMapScreen
import com.google.android.gms.maps.model.LatLng


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isViewingSharedRoute = true // Simule isso como se viesse de um link de compartilhamento


        setContent {
            RouteShareTheme {
                Surface (modifier = Modifier.fillMaxSize())
                {
                    if (isViewingSharedRoute)
                    {
                        SharedMapScreen(
                            sharedLatLng = LatLng(-15.793889, -47.882778),
                            onExit = { finish() }
                        )
                    } else  {
                        MapScreen()
                    }
                }
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RouteShareTheme {
        Greeting("Android")
    }
}