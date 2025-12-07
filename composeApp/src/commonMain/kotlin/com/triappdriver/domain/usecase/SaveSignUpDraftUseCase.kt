package com.triappdriver.domain.usecase

import com.triappdriver.domain.model.SignUpDomainModel
import com.triappdriver.local.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveSignUpDraftUseCase(
    private val appPreferences: AppPreferences
) {
    suspend operator fun invoke(model: SignUpDomainModel) = withContext(Dispatchers.Default) {
        appPreferences.saveSignUpDraft(model)
    }
}