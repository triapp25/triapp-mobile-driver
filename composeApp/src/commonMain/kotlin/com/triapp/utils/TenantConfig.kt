package com.triapp.utils

data class TenantConfig(
    val tenantId: String,
    val baseUrl: String, // URL base para a API Ktorfit (pode ser diferente por tenant)
    val firebaseApiKey: String, // Se necessário para APIs externas (o Firebase SDK usa seus próprios arquivos)
    val primaryColorHex: String // Exemplo de customização de UI/Theming
)

// O serviço que fornecerá a configuração atual
interface TenantService {
    fun getCurrentTenantConfig(): TenantConfig
    fun switchTenant(newTenantId: String)
    fun currentThemeColor(): String = getCurrentTenantConfig().primaryColorHex
}

expect fun getTenantService(context: Any? = null): TenantService