package com.triapp.domain.usecase

import com.triapp.domain.model.SignUpDomainModel
import com.triapp.local.AppPreferences
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