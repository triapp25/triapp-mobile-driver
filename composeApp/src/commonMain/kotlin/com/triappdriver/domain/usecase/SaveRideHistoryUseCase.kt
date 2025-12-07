package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.RideRepository
import com.triappdriver.domain.model.RideHistoryDomainModel
import kotlinx.coroutines.flow.Flow

class SaveRideUseCase(private val repository: RideRepository) {
    suspend operator fun invoke(ride: RideHistoryDomainModel) {
        // Validações de negócio podem ser feitas aqui antes de salvar
        if (ride.origin.isNotBlank() && ride.destination.isNotBlank()) {
            repository.saveRide(ride)
        }
    }
}