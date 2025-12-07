package com.triappdriver.domain.usecase

import com.triappdriver.domain.model.SignUpDomainModel
import com.triappdriver.local.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * UseCase para carregar o estado de rascunho de registro.
 */
class GetSignUpDraftUseCase(
    private val appPreferences: AppPreferences,
    private val json: Json
) {
    operator fun invoke(): Flow<SignUpDomainModel?> {
        return appPreferences.observeSignUpDraft().map { jsonString ->
            jsonString?.let {
                json.decodeFromString<SignUpDomainModel>(it)
            }
        }
    }
}