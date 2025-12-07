package com.triappdriver.presentation.feature.profile

import androidx.lifecycle.viewModelScope
import com.triappdriver.domain.model.ProfileDomainModel
import com.triappdriver.domain.model.WalletDomainModel.CardOption
import com.triappdriver.domain.usecase.CardOptionsUseCase
import com.triappdriver.domain.usecase.GetRideHistoryUseCase
import com.triappdriver.domain.usecase.GetSignUpDraftUseCase
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val cardUseCase: CardOptionsUseCase,
    private val rideUseCase: GetRideHistoryUseCase,
    private val userUseCase: GetSignUpDraftUseCase,
    private val firebaseManager: FirebaseAuthManager
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
                viewModelScope.launch {
                    firebaseManager.signOut()
                    sendEffect(ProfileEffect.LoggedOut)
                }
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
