package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.RideRepository
import com.triappdriver.domain.model.RideHistoryDomainModel
import kotlinx.coroutines.flow.Flow

class GetRideHistoryUseCase(private val repository: RideRepository) {
    operator fun invoke(): Flow<List<RideHistoryDomainModel>> {
        return repository.getRideHistory()
    }
}