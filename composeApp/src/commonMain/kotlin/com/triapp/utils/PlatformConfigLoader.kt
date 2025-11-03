package com.triapp.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

expect class PlatformConfigLoader {
    /**
     * Carrega o conteúdo do arquivo 'config.json' como uma String.
     */
    suspend fun loadConfigJson(): String
}

// Data class auxiliar para deserializar o JSON
@Serializable
data class AppEnvConfig(
    @SerialName("api_base_url") val apiBaseUrl: String,
    @SerialName("tenant_id") val tenantId: String
)