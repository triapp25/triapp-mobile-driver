package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.DataRepository
import com.triappdriver.data.repository.RatingRepository
import com.triappdriver.domain.model.RatingDomainModel
import com.triappdriver.presentation.feature.rating.RatingEffect
import com.triappdriver.utils.getCrashlyticsService

class SendRatingUseCase(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(model: RatingDomainModel): Result<Unit> {
        return try {
            repository.sendRatingToApi(model)
            repository.markAsRatedInLocalDb(model.tripId)
            Result.success(Unit)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}
