package com.triapp.domain.usecase

import com.triapp.data.repository.RideRepository
import com.triapp.domain.model.RideHistoryDomainModel
import kotlinx.coroutines.flow.Flow

class GetRideHistoryUseCase(private val repository: RideRepository) {
    operator fun invoke(): Flow<List<RideHistoryDomainModel>> {
        return repository.getRideHistory()
    }
}