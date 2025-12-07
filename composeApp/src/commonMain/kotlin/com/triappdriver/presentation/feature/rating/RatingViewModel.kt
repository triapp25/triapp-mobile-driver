package com.triappdriver.presentation.feature.rating

import androidx.lifecycle.viewModelScope
import com.triappdriver.domain.model.RatingArgs
import com.triappdriver.domain.model.RatingDomainModel
import com.triappdriver.domain.usecase.CreateLocalRatingUseCase
import com.triappdriver.domain.usecase.SendRatingUseCase
import com.triappdriver.presentation.BaseViewModel
import kotlinx.coroutines.launch

class RatingViewModel(
    private val sendRatingUseCase: SendRatingUseCase,
    private val createRatingUseCase: CreateLocalRatingUseCase,
    args: RatingArgs
) : BaseViewModel<RatingDomainModel, RatingIntent, RatingEffect>(
    // Inicializa o estado com os dados da corrida passados via Args
    initialState = RatingDomainModel(
        tripId = args.tripId,
        riderName = args.riderName,
        driverPhotoUrl = args.driverPhotoUrl,
        tripPrice = args.price,
        tripTime = args.time,
        step = RatingStep.Rating
    )
) {

    init {
        // REQUISITO 1: Ao inicializar, salvar no Room como pendente
        initializeRatingSession()
    }

    private fun initializeRatingSession() {
        viewModelScope.launch {
            createRatingUseCase.invoke(state.value)
        }
    }

    override fun processIntent(intent: RatingIntent) {
        when (intent) {
            is RatingIntent.SelectRating -> {
                updateState { it.copy(rating = intent.rating) }
                // Opcional: Salvar rascunho a cada interação se desejar persistência em tempo real
                // saveDraft()
            }

            RatingIntent.Submit -> {
                if (state.value.rating > 0) {
                    submitRating()
                } else {
                    sendEffect(RatingEffect.ShowToast("Por favor, selecione uma nota de 1 a 5."))
                }
            }

            RatingIntent.Skip -> {
                sendEffect(RatingEffect.Finished)
            }
        }
    }

    private fun submitRating() {
        viewModelScope.launch {
            sendRatingUseCase.invoke(state.value)
                .onFailure {
                    // Tratamento de erro (pode exibir toast ou diálogo de retry)
                }.also {
                    sendEffect(RatingEffect.Finished)
                }
        }
    }
}