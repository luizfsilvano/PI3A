from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import uuid
import json
from typing import Dict, List

app = FastAPI()

class ConnectionManager:
    def __init__(self):
        # Dicionário para armazenar conexões: { "share_id": [websocket1, websocket2] }
        self.active_connections: Dict[str, List[WebSocket]] = {}

    async def connect(self, websocket: WebSocket, share_id: str):
        await websocket.accept()
        if share_id not in self.active_connections:
            self.active_connections[share_id] = []
        self.active_connections[share_id].append(websocket)

    def disconnect(self, websocket: WebSocket, share_id: str):
        if share_id in self.active_connections:
            self.active_connections[share_id].remove(websocket)
            if not self.active_connections[share_id]:
                del self.active_connections[share_id]

    async def broadcast(self, message: str, share_id: str):
        if share_id in self.active_connections:
            for connection in self.active_connections[share_id]:
                await connection.send_text(message)

manager = ConnectionManager()

@app.post("/api/share/start")
async def start_sharing():
    """Endpoint para o "Compartilhador" iniciar uma sessão e obter um ID."""
    share_id = str(uuid.uuid4())
    return {"share_id": share_id}

@app.websocket("/ws/share/{share_id}")
async def websocket_endpoint(websocket: WebSocket, share_id: str):
    """
    Endpoint de WebSocket.
    - O "Compartilhador" conecta e envia atualizações de localização.
    - O "Visualizador" conecta e apenas escuta essas atualizações.
    """
    await manager.connect(websocket, share_id)
    try:
        while True:
            # Aguarda por dados (do "Compartilhador")
            data = await websocket.receive_text()
            # Retransmite os dados para todos os "Visualizadores" da mesma sessão
            await manager.broadcast(data, share_id)
    except WebSocketDisconnect:
        manager.disconnect(websocket, share_id)
        print(f"Cliente desconectado da sessão: {share_id}")