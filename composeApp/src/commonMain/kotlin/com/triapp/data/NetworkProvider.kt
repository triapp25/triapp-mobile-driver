package com.triapp.data

class NetworkProvider(
    private val apiService: ApiService,
) {

    private var usingWebSocket = false

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
