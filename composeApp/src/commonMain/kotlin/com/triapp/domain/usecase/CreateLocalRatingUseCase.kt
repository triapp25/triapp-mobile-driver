package com.triapp.domain.usecase

import com.triapp.data.repository.RatingRepository
import com.triapp.domain.model.RatingDomainModel
import com.triapp.utils.getCrashlyticsService

class CreateLocalRatingUseCase(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(model: RatingDomainModel): Result<Unit> {
        return try {
            repository.saveOrUpdateLocalRating(model)
            Result.success(Unit)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}
