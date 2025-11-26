package com.triapp.presentation.feature.profile

import com.triapp.domain.model.ProfileDomainModel
import com.triapp.presentation.BaseViewModel

class ProfileViewModel :
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
        }
    }
}
