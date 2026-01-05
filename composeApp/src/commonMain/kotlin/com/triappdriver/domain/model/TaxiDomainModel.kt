package com.triappdriver.domain.model

import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RideRiderDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO

sealed class TaxiState {
    object Idle : TaxiState()
    data class Online(val rider: RiderFirebaseDTO) : TaxiState()
    data class Started(val documentId: String) : TaxiState()
    data class Update(
        val driverDTO: RideDriverDTO?,
        val riderDTO: RideRiderDTO?,
        val status: String
    ) : TaxiState()

    object Completed : TaxiState()
    data class Error(val message: String) : TaxiState()
}