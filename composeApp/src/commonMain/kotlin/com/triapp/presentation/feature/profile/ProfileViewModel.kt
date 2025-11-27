package com.triapp.presentation.feature.profile

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.ProfileDomainModel
import com.triapp.domain.model.WalletDomainModel.CardOption
import com.triapp.domain.usecase.CardOptionsUseCase
import com.triapp.domain.usecase.GetRideHistoryUseCase
import com.triapp.domain.usecase.GetSignUpDraftUseCase
import com.triapp.presentation.BaseViewModel
import kotlinx.coroutines.launch

class ProfileViewModel(
    val cardUseCase: CardOptionsUseCase,
    val rideUseCase: GetRideHistoryUseCase,
    val userUseCase: GetSignUpDraftUseCase
) :
    BaseViewModel<ProfileDomainModel, ProfileIntent, ProfileEffect>(
        initialState = ProfileDomainModel()
    ) {

    override fun processIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.Navigate -> {
                updateState { it.copy(currentStep = intent.step) }
            }

            ProfileIntent.Back -> {
                updateState { it.copy(currentStep = ProfileStep.Main) }
            }

            ProfileIntent.Logout -> {
                sendEffect(ProfileEffect.LoggedOut)
            }

            is ProfileIntent.CreateCard -> {
                viewModelScope.launch {
                    cardUseCase.invoke(intent.walletDomainModel.copy(cardOption = CardOption.CREATE))
                }
            }
            is ProfileIntent.DeleteCard -> {
                viewModelScope.launch {
                    cardUseCase.invoke(intent.walletDomainModel.copy(cardOption = CardOption.DELETE))
                }
            }
            is ProfileIntent.UpdateCard -> {
                viewModelScope.launch {
                    cardUseCase.invoke(intent.walletDomainModel.copy(cardOption = CardOption.UPDATED))
                }
            }
        }
    }
}
