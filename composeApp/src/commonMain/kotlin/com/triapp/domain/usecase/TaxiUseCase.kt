package com.triapp.domain.usecase

import com.triapp.data.firestore.listenDocumentUntilDone
import com.triapp.data.repository.DataRepository
import com.triapp.domain.model.Coordinate
import com.triapp.domain.model.TaxiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TaxiUseCase(
    private val repository: DataRepository
) {

    fun startTaxiFlow(
        riderId: String,
        pickup: Coordinate,
        pickupAddress: String,
        dropoff: Coordinate,
        dropoffAddress: String,
        riderNote: String
    ): Flow<TaxiState> = flow {

        emit(TaxiState.Idle)

        // 1) Chama o REST → inicia corrida → recebe documentId
        val documentId = repository.rideRequest(
            riderId, pickup, pickupAddress,
            dropoff, dropoffAddress, riderNote
        )
        emit(TaxiState.Started(documentId))

        // 2) Inicia listener Firestore
        listenDocumentUntilDone(documentId)
            .catch { e -> emit(TaxiState.Error(e.message ?: "Erro desconhecido")) }
            .collect { result ->
                emit(TaxiState.Update(result.driver, result.status))

                if (result.status == "COMPLETED") {
                    emit(TaxiState.Completed)
                }
            }

    }
}
