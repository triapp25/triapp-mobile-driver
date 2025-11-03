package com.triapp.domain.usecase

import com.triapp.domain.model.SignUpDomainModel
import com.triapp.local.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveSignUpDraftUseCase(
    private val appPreferences: AppPreferences
) {
    suspend operator fun invoke(model: SignUpDomainModel) = withContext(Dispatchers.Default) {
        appPreferences.saveSignUpDraft(model)
    }
}