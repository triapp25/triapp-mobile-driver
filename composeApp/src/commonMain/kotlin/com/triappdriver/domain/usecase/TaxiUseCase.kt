package com.triappdriver.domain.usecase

import com.triappdriver.data.dtos.LocationDTO
import com.triappdriver.data.firestore.FirestoreRepository
import com.triappdriver.data.firestore.listenDocumentUntilDone
import com.triappdriver.data.firestore.listenOnline
import com.triappdriver.data.repository.DataRepository
import com.triappdriver.domain.model.RideSnapshotDTO
import com.triappdriver.domain.model.TaxiState
import com.triappdriver.domain.model.fromMap
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

    /**
     * Listener principal da corrida
     * Sempre emite snapshot completo
     */
    fun startTaxiFlow(tripId: String): Flow<TaxiState> = flow {
        listenDocumentUntilDone(
            firestoreRepository,
            tripId
        )
            .catch { e ->
                emit(TaxiState.Error(e.message ?: "Erro desconhecido"))
            }
            .collect { result ->
                emit(
                    TaxiState.Update(
                        snapshot = result.snapshot
                    )
                )
            }
    }

    /**
     * Atualizações de status feitas pelo motorista
     */
    suspend fun updateTripStatus(
        tripId: String,
        newStatus: String,
        location: LocationDTO?,
        etaMinutes: Int,
        distanceMeters: Int
    ) {
        try {
            when (newStatus) {
                "ACCEPTED" ->
                    dataRepository.rideAccepted(tripId, location!!)

                "REJECTED" ->
                    dataRepository.rideRejected(tripId)

                "ONGOING" ->
                    dataRepository.rideOnGoing(tripId, location!!)

                "COMPLETED" ->
                    dataRepository.rideCompleted(
                        tripId,
                        location!!,
                        etaMinutes.toDouble(),
                        distanceMeters.toDouble()
                    )
            }
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            throw e
        }
    }

    /**
     * Listener de novas corridas (motorista online)
     */
    fun enabledOnline(): Flow<TaxiState> = flow {
        listenOnline(firestoreRepository, firebaseAuthManager)
            .catch { e ->
                emit(TaxiState.Error(e.message ?: "Erro desconhecido"))
            }
            .collect { result ->
                result?.let {
                    emit(TaxiState.Online(it))
                }
            }
    }
}