# RouteShare

Um aplicativo full-stack de compartilhamento de localização e rotas em tempo real, desenvolvido como um projeto de estudo. A aplicação consiste em um cliente Android nativo e um backend em Python com FastAPI.

## 📖 Descrição

RouteShare permite que um usuário compartilhe sua localização em tempo real, gerando um link único. Qualquer pessoa com o link pode abrir o aplicativo e visualizar a localização do compartilhador se movendo em um mapa, similar à funcionalidade de "Acompanhar Viagem" de aplicativos como Uber e iFood.

O projeto também demonstra a exibição de rotas estáticas (mockadas) no mapa, fornecendo uma base para futuras funcionalidades de planejamento de viagem.

## ✨ Funcionalidades Principais

  * **Compartilhamento de Localização em Tempo Real:** Um usuário (Compartilhador) pode iniciar uma sessão de compartilhamento.
  * **Visualização em Tempo Real:** Outro usuário (Espectador) pode usar um link para visualizar a localização do compartilhador em um mapa, com o marcador se movendo e rotacionando em tempo real.
  * **Comunicação via WebSocket:** Utiliza WebSockets para comunicação de baixa latência entre o cliente e o servidor.
  * **Mapa Interativo:** Construído com o Google Maps e Jetpack Compose, permitindo uma interface moderna e reativa.
  * **Rota Estática:** Demonstra como carregar e exibir uma rota pré-definida no mapa.
  * **Deep Linking:** O aplicativo responde a URLs customizadas para abrir diretamente na tela de visualização.

## 🏗️ Arquitetura

O sistema é dividido em duas partes principais:

1.  **Backend (Python/FastAPI):**

      * Responsável por gerenciar as sessões de compartilhamento.
      * Recebe as coordenadas do usuário "Compartilhador".
      * Utiliza um `ConnectionManager` para gerenciar as conexões WebSocket.
      * Retransmite (`broadcast`) as coordenadas em tempo real para todos os "Espectadores" conectados na mesma sessão.

2.  **Frontend (Android/Jetpack Compose):**

      * **Fluxo do Compartilhador:**
          * Utiliza o `FusedLocationProviderClient` para obter a localização precisa do dispositivo.
          * Executa um `Foreground Service` para garantir que a localização continue sendo enviada mesmo com o app em segundo plano.
          * Conecta-se ao backend via WebSocket e envia continuamente os dados de `latitude`, `longitude`.
      * **Fluxo do Espectador:**
          * É iniciado através de um deep link (`https://routeshare.example.com/view/...`).
          * Extrai o ID da sessão a partir do link.
          * Conecta-se ao WebSocket do backend para receber os dados de localização.
          * Atualiza a UI em tempo real, movendo e rotacionando um marcador customizado no mapa.

## 🛠️ Tecnologias Utilizadas

### Backend

  * **Python 3**
  * **FastAPI:** Para a criação da API e do endpoint WebSocket.
  * **Uvicorn:** Como servidor ASGI para rodar o FastAPI.
  * **WebSockets:** Para a comunicação em tempo real.

### Frontend (Android)

  * **Kotlin**
  * **Jetpack Compose:** Para a construção da UI de forma declarativa e moderna.
  * **Google Maps Compose Library:** Para integração nativa do Google Maps com Compose.
  * **Maps KTX and Utils:** Para decodificar polilinhas e outras utilidades.
  * **Play Services Location:** Para obter a localização do dispositivo (`FusedLocationProviderClient`).
  * **OkHttp:** Como cliente para a comunicação WebSocket.
  * **Accompanist (Permissions):** Para facilitar o gerenciamento de permissões em tempo de execução.

## 🧪 Como Testar

1.  **Inicie dois emuladores** ou um emulador e um celular físico.
2.  **No Dispositivo A (Compartilhador):**
      * Abra o app. A `MapScreen` será exibida.
      * Clique em "Compartilhar".
      * Na caixa de diálogo de compartilhamento, copie o link gerado.
3.  **No Dispositivo B (Espectador):**
      * Abra o terminal do Android Studio.
      * Use o comando `adb -s emulator-(id_do_celular) shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "https://routeshare.example.com/view/(id_da_sessão)"` para simular o clique no link.
      * O aplicativo deve abrir diretamente na tela "Acompanhando rota".
4.  **Observe a Mágica:**
      * No Dispositivo A, use os "Extended Controls" (`...`) do emulador para definir ou simular uma rota na aba "Location".
      * O marcador no Dispositivo B deve começar a se mover em tempo real.

-----

*Projeto desenvolvido como parte de um estudo acadêmico para a matéria PI3A do IESB.*

**Participantes:** 
- Luiz Felipe Sampaio Silvano - 2312130190
- Rafael Bastos Lancellotti - 2222130007
- Gabriel Moraes da Silva - 2312130210
- João Pedro Souza Nogueira - 2212130062
