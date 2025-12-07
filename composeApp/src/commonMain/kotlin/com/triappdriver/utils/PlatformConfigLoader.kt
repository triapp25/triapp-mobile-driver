package com.triappdriver.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

expect class PlatformConfigLoader {
    suspend fun loadConfigJson(): String
}

// Data class auxiliar para deserializar o JSON
@Serializable
data class AppEnvConfig(
    @SerialName("api_base_url") val apiBaseUrl: String,
    @SerialName("tenant_id") val tenantId: String
)