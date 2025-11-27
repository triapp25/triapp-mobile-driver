package com.triapp.domain.usecase

import com.triapp.data.repository.DataRepository
import com.triapp.domain.model.RatingDomainModel
import com.triapp.utils.getCrashlyticsService

class SendRatingUseCase(
    private val dataRepository: DataRepository,
) {
    suspend operator fun invoke(model: RatingDomainModel): Result<Unit> {
        return try {
            dataRepository.sendRating(model)
            Result.success(Unit)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}