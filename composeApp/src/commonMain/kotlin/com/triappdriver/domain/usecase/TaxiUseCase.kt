package com.triappdriver.domain.usecase

import com.triappdriver.data.dtos.LocationDTO
import com.triappdriver.data.firestore.FirestoreRepository
import com.triappdriver.data.firestore.listenDocumentUntilDone
import com.triappdriver.data.firestore.listenOnline
import com.triappdriver.data.repository.DataRepository
import com.triappdriver.domain.model.TaxiState
import com.triappdriver.utils.FirebaseAuthManager
import com.triappdriver.utils.getCrashlyticsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TaxiUseCase(
    private val firestoreRepository: FirestoreRepository,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val dataRepository: DataRepository
) {

    // 1. Ouve as mudanças de status vindas do Backend/Firestore
    fun startTaxiFlow(tripId: String): Flow<TaxiState> = flow {
        listenDocumentUntilDone(
            firestoreRepository,
            tripId
        )
            .catch { e -> emit(TaxiState.Error(e.message ?: "Erro desconhecido")) }
            .collect { result ->
                emit(TaxiState.Update(result.driver, result.rider, result.status))

                if (result.status == "COMPLETED") {
                    emit(TaxiState.Completed)
                }
            }
    }

    // 2. Funções para o Motorista alterar o status no Firestore
    suspend fun updateTripStatus(
        tripId: String,
        newStatus: String,
        location: LocationDTO?,
        etaMinutes: Int,
        distanceMeters: Int
    ) {
        try {
            if (newStatus == "COMPLETED") {
                dataRepository.rideCompleted(tripId, location!!, etaMinutes.toDouble(), distanceMeters.toDouble())
            } else if (newStatus == "ACCEPTED") {
                dataRepository.rideAccepted(tripId, location!!)
            } else if (newStatus == "REJECTED") {
                dataRepository.rideRejected(tripId)
            } else if (newStatus == "ONGOING") {
                dataRepository.rideOnGoing(tripId, location!!)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }

    fun enabledOnline(): Flow<TaxiState> = flow {
        listenOnline(firestoreRepository, firebaseAuthManager)
            .catch { e -> emit(TaxiState.Error(e.message ?: "Erro desconhecido")) }
            .collect { result ->
                // Emite o estado atualizado com os dados vindos do banco
                result?.let { emit(TaxiState.Online(it)) }

            }
    }
}