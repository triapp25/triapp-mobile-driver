package com.triapp.data

import com.triapp.utils.TenantConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.URLBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json


data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
)

class WebSocketProvider(private val config: TenantConfig) {

    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null

    suspend fun connect() {
        if (session == null) {
            client.webSocketSession(
                host = URLBuilder(config.baseUrl).host,
                path = "/realtime",
                block = {
                    header("X-Tenant-Id", config.tenantId)
                }

            )
        }
    }

    suspend fun sendLocation(latitude: Double, longitude: Double) {
        val location = LocationUpdate(latitude, longitude)
        val jsonData = Json.encodeToString(location)
        session?.send(Frame.Text(jsonData))
        println("Enviado: $jsonData")
    }

    suspend fun listen(onLocationReceived: (LocationUpdate) -> Unit) {
        val s = session ?: return
        try {
            s.incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    println("Recebido: $text")
                    try {
                        val data = Json.decodeFromString<LocationUpdate>(text)
                        onLocationReceived(data)
                    } catch (_: Exception) {
                        println("Ignorando mensagem não relacionada a localização.")
                    }
                }
            }
        } catch (e: Exception) {
            println("Erro no WebSocket: ${e.message}")
        }
    }

    suspend fun disconnect() {
        session?.close()
        session = null
        println("WebSocket desconectado")
    }
}
