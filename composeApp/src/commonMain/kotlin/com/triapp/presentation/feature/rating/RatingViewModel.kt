package com.triapp.presentation.feature.rating

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.RatingDomainModel
import com.triapp.domain.usecase.SendRatingUseCase
import com.triapp.presentation.BaseViewModel
import kotlinx.coroutines.launch

class RatingViewModel(
    val useCase: SendRatingUseCase
) :
    BaseViewModel<RatingDomainModel, RatingIntent, RatingEffect>(
        initialState = RatingDomainModel()
    ) {

    override fun processIntent(intent: RatingIntent) {
        when (intent) {

            is RatingIntent.SelectRating -> {
                updateState { it.copy(rating = intent.rating) }
            }

            is RatingIntent.UpdateComment -> {
                updateState { it.copy(comment = intent.comment) }
            }

            is RatingIntent.ToggleTag -> {
                updateState {
                    val updated = if (state.value.selectedTags.contains(intent.tag)) {
                        state.value.selectedTags - intent.tag
                    } else {
                        state.value.selectedTags + intent.tag
                    }
                    it.copy(selectedTags = updated)
                }
            }

            is RatingIntent.SelectTip -> {
                updateState { it.copy(selectedTip = intent.tip) }
            }

            RatingIntent.Submit -> {
                if (state.value.rating > 0) {
                    sendRating()
                } else {
                    sendEffect(RatingEffect.ShowToast("Select a rating"))
                }
            }

            RatingIntent.Skip -> {
                sendEffect(RatingEffect.Finished)
            }
        }
    }

    private fun sendRating() {
        viewModelScope.launch {
            useCase.invoke(state.value)
                .onFailure {
                    // Processar resposta em outro momento
                }.also {
                    sendEffect(RatingEffect.Finished)
                }
        }
    }
}
