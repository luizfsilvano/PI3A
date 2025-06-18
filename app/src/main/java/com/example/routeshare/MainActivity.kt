package com.example.routeshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.routeshare.ui.screens.MapScreen
import com.example.routeshare.ui.screens.SharedMapScreen
import com.example.routeshare.ui.theme.RouteShareTheme

class MainActivity : ComponentActivity() {

    // 1. Criamos um estado para guardar o ID do compartilhamento.
    private val shareIdState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Processa o Intent que iniciou o app (quando ele estava fechado).
        handleIntent(intent)

        setContent {
            RouteShareTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // O `by` faz o Compose observar as mudanças neste estado.
                    val shareId by shareIdState

                    if (shareId != null) {
                        // Se um shareId existe, mostra a tela do visualizador.
                        SharedMapScreen(
                            shareId = shareId!!, // Usamos !! porque já checamos que não é nulo.
                            onExit = { finish() }
                        )
                    } else {
                        // Caso contrário, mostra a tela principal.
                        MapScreen()
                    }
                }
            }
        }
    }

    // 3. Este método é chamado quando o app já está aberto e recebe um novo Intent.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Processa o novo Intent que chegou.
        handleIntent(intent)
    }

    // 4. Uma função centralizada para extrair o ID do Intent e atualizar o estado.
    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            // Se a ação for de visualizar um link, pega o ID e atualiza o estado.
            // A UI do Compose irá reagir automaticamente a essa mudança.
            shareIdState.value = intent.data?.lastPathSegment
        }
    }
}