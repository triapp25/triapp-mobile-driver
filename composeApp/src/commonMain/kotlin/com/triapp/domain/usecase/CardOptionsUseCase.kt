package com.triapp.domain.usecase

import com.triapp.data.repository.DataRepository
import com.triapp.domain.model.WalletDomainModel
import com.triapp.utils.getCrashlyticsService

class CardOptionsUseCase(
    private val dataRepository: DataRepository,
) {
    suspend operator fun invoke(model: WalletDomainModel): Result<WalletDomainModel> {
        return try {
            val result = dataRepository.cardOptions(model)
            Result.success(result)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}