package com.triapp.domain.usecase

import com.triapp.data.repository.RatingRepository
import com.triapp.domain.model.RatingDomainModel
import com.triapp.utils.getCrashlyticsService
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