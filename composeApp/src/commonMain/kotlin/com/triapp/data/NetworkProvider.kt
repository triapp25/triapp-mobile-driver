package com.triapp.data

class NetworkProvider(
    private val apiService: ApiService,
    private val webSocketProvider: WebSocketProvider
) {

    private var usingWebSocket = false

    suspend fun startRealtime() {
        usingWebSocket = true
        webSocketProvider.connect()
    }

    suspend fun stopRealtime() {
        usingWebSocket = false
        webSocketProvider.disconnect()
    }

    suspend fun getData(): Any {
        return if (usingWebSocket) {
            // WebSocket já lida com eventos de push — pode ser vazio ou callback-driven
            "Listening via WebSocket"
        } else {
            // REST fallback (polling)
            apiService.getProducts()
        }
    }
}
