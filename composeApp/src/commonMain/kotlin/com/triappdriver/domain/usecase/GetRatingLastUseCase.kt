package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.RatingRepository
import com.triappdriver.domain.model.RatingDomainModel
import com.triappdriver.utils.getCrashlyticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetRatingLastUseCase(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(): Result<RatingDomainModel?> {
        return try {
            Result.success(repository.getLastPendingRating())
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}