package com.triappdriver.local

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.triappdriver.domain.model.SignUpDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString // <--- ADICIONE ISTO

class AppPreferences(
    private val settings: ObservableSettings,
    private val json: Json
) {

    private val LAST_TENANT_ID_KEY = "last_tenant_id"
    private val IS_DARK_MODE_KEY = "is_dark_mode"
    private val SIGN_UP_DRAFT_KEY = "sign_up_draft"
    private val DRIVER_ACTIVE = "diver_activie"

    fun observeLastTenantId(): Flow<String?> =
        settings.getStringOrNullFlow(LAST_TENANT_ID_KEY)

    suspend fun setLastTenantId(id: String) {
        settings.putString(LAST_TENANT_ID_KEY, id)
    }

    fun isDarkMode(): Flow<Boolean> =
        settings.getBooleanFlow(IS_DARK_MODE_KEY, defaultValue = false)

    suspend fun toggleDarkMode(isDark: Boolean) {
        settings.putBoolean(IS_DARK_MODE_KEY, isDark)
    }

    fun isDriverActive(): Flow<Boolean> =
        settings.getBooleanFlow(DRIVER_ACTIVE, defaultValue = false)

    suspend fun driverActive(isDriverActive: Boolean) {
        settings.putBoolean(DRIVER_ACTIVE, isDriverActive)
    }

    fun observeSignUpDraft(): Flow<String?> =
        settings.getStringOrNullFlow(SIGN_UP_DRAFT_KEY)

    suspend fun saveSignUpDraft(model: SignUpDomainModel) {
        val jsonString = json.encodeToString(model)
        settings.putString(SIGN_UP_DRAFT_KEY, jsonString)
    }

    suspend fun clearSignUpDraft() {
        settings.remove(SIGN_UP_DRAFT_KEY)
    }
}

expect fun provideObservableSettings(context: Any? = null): ObservableSettings
