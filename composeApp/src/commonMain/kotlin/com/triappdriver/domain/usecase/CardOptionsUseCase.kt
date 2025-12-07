package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.DataRepository
import com.triappdriver.domain.model.WalletDomainModel
import com.triappdriver.utils.getCrashlyticsService

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