package com.triappdriver.domain.usecase

import com.triappdriver.data.firestore.listenDocumentUntilDone
import com.triappdriver.data.repository.DataRepository
import com.triappdriver.domain.model.TaxiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TaxiUseCase(
    private val repository: DataRepository
) {

    private val db = Firebase.firestore

    // 1. Ouve as mudanças de status vindas do Backend/Firestore
    fun startTaxiFlow(tripId: String): Flow<TaxiState> = flow {
        listenDocumentUntilDone(tripId)
            .catch { e -> emit(TaxiState.Error(e.message ?: "Erro desconhecido")) }
            .collect { result ->
                // Emite o estado atualizado com os dados vindos do banco
                emit(TaxiState.Update(result.driver, result.status))

                if (result.status == "COMPLETED") {
                    emit(TaxiState.Completed)
                }
            }
    }

    // 2. Funções para o Motorista alterar o status no Firestore
    suspend fun updateTripStatus(tripId: String, newStatus: String) {
        try {
            // Atualiza apenas o campo status
            db.collection("trip_status").document(tripId)
                .update(mapOf("status" to newStatus))
        } catch (e: Exception) {
            // Tratar erro de conexão se necessário
            e.printStackTrace()
        }
    }
}