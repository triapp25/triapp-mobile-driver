package com.triapp.domain.usecase

import com.triapp.data.firestore.listenDocumentUntilDone
import com.triapp.data.repository.DataRepository
import com.triapp.domain.model.TaxiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

class TaxiUseCase(
    private val repository: DataRepository
) {

    fun startTaxiFlow(): Flow<TaxiState> = flow {

        emit(TaxiState.Idle)

        // 1) Chama o REST → inicia corrida → recebe documentId
        val documentId = repository.startTaxi()
        emit(TaxiState.Started(documentId))

        // 2) Inicia listener Firestore
        listenDocumentUntilDone(documentId)
            .catch { e -> emit(TaxiState.Error(e.message ?: "Erro desconhecido")) }
            .collect { result ->
                emit(TaxiState.Update(result.data, result.status))

                if (result.status == "done") {
                    emit(TaxiState.Completed)
                }
            }

    }
}
