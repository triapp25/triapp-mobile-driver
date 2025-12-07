package com.triappdriver.domain.model

import com.triappdriver.data.dtos.RideDriverDTO

sealed class TaxiState {
    object Idle : TaxiState()
    data class Started(val documentId: String) : TaxiState()
    data class Update(val driverDTO: RideDriverDTO, val status: String) : TaxiState()
    object Completed : TaxiState()
    data class Error(val message: String) : TaxiState()
}