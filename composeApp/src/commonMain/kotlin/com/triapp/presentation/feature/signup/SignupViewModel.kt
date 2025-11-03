package com.triapp.presentation.feature.signup

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.SignUpDomainModel
import com.triapp.domain.usecase.SaveSignUpDraftUseCase
import com.triapp.presentation.BaseViewModel
import kotlinx.coroutines.launch

class SignupViewModel(
    //private val saveSignUpDraftUseCase: SaveSignUpDraftUseCase
) :
    BaseViewModel<SignUpDomainModel, SignupIntent, SignupEffect>(
        initialState = SignUpDomainModel()
    ) {

    override fun processIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.EnterFullName -> updateState { it.copy(fullName = intent.name) }
            is SignupIntent.EnterEmail -> updateState { it.copy(email = intent.email) }
            is SignupIntent.EnterPhone -> updateState { it.copy(phone = intent.phone) }
            is SignupIntent.EnterPassword -> updateState { it.copy(password = intent.password) }
            is SignupIntent.EnterConfirmPassword -> updateState { it.copy(confirmPassword = intent.password) }
            is SignupIntent.UploadProfilePhoto -> updateState { it.copy(profilePhotoPath = intent.path) }
            is SignupIntent.UploadIdDocument -> updateState { it.copy(idDocumentPath = intent.path) }
            SignupIntent.NextStep -> goToNextStep()
            SignupIntent.PreviousStep -> goToPreviousStep()
        }
    }

    private fun goToNextStep() {
        val next = when (state.value.currentStep) {
            RegistrationStep.PersonalInfo -> RegistrationStep.ContactDetails
            RegistrationStep.ContactDetails -> RegistrationStep.SecureAccount
            RegistrationStep.SecureAccount -> RegistrationStep.DocumentVerification
            RegistrationStep.DocumentVerification -> RegistrationStep.AllSet
            RegistrationStep.AllSet -> RegistrationStep.AllSet
        }

        updateState { it.copy(currentStep = next) }

        if (next == RegistrationStep.AllSet) {
            //viewModelScope.launch { saveSignUpDraftUseCase.invoke(state.value) }

            sendEffect(SignupEffect.RegisterSuccess(state.value))
        }
    }

    private fun goToPreviousStep() {
        val prev = when (state.value.currentStep) {
            RegistrationStep.PersonalInfo -> RegistrationStep.PersonalInfo
            RegistrationStep.ContactDetails -> RegistrationStep.PersonalInfo
            RegistrationStep.SecureAccount -> RegistrationStep.ContactDetails
            RegistrationStep.DocumentVerification -> RegistrationStep.SecureAccount
            RegistrationStep.AllSet -> RegistrationStep.DocumentVerification
        }

        updateState { it.copy(currentStep = prev) }
    }
}
