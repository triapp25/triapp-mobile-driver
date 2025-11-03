package com.triapp.utils

import android.content.Context
import kotlinx.serialization.json.Json

private var cachedService: TenantService? = null

class AndroidTenantService(private val context: Context) : TenantService {
    private var currentConfig: TenantConfig = loadConfig()

    private fun loadConfig(): TenantConfig {
        val jsonStr = context.assets.open("config.json")
            .bufferedReader()
            .use { it.readText() }

        val parsed = Json.decodeFromString<TenantConfig>(jsonStr)
        return parsed
    }

    override fun getCurrentTenantConfig(): TenantConfig = currentConfig

    override fun switchTenant(newTenantId: String) {
        currentConfig = currentConfig.copy(tenantId = newTenantId)
    }
}

actual fun getTenantService(context: Any?): TenantService {
    require(context is Context) { "Android context is required" }
    if (cachedService == null) {
        cachedService = AndroidTenantService(context)
    }
    return cachedService!!
}