package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.RatingRepository
import com.triappdriver.domain.model.RatingDomainModel
import com.triappdriver.utils.getCrashlyticsService

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
