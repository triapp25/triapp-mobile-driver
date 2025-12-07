package com.triappdriver.utils

import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile


class IosTenantService : TenantService {
    private var currentConfig: TenantConfig = loadConfig()

    private fun loadConfig(): TenantConfig {
        val path = NSBundle.mainBundle.pathForResource("config", "json")!!
        val data = NSData.dataWithContentsOfFile(path)!!
        val jsonString = NSString.create(data, NSUTF8StringEncoding) as String
        val parsed = Json.decodeFromString<TenantConfig>(jsonString)
        return parsed
    }

    override fun getCurrentTenantConfig(): TenantConfig = currentConfig

    override fun switchTenant(newTenantId: String) {
        currentConfig = currentConfig.copy(tenantId = newTenantId)
    }
}

actual fun getTenantService(context: Any?): TenantService {
    return IosTenantService()
}