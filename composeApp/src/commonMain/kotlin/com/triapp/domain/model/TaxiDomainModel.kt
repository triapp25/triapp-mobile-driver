package com.triapp.domain.model

sealed class TaxiState {
    object Idle : TaxiState()
    data class Started(val documentId: String) : TaxiState()
    data class Update(val data: Map<String, Any?>, val status: String) : TaxiState()
    object Completed : TaxiState()
    data class Error(val message: String) : TaxiState()
}
