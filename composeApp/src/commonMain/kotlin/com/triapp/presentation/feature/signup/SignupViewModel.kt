package com.triapp.presentation.feature.signup

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.SignUpDomainModel as DomainSignUpModel
import com.triapp.presentation.BaseViewModel
import com.triapp.utils.FirebaseAuthManager
import kotlinx.coroutines.launch

class SignupViewModel(
    // add use-cases / repos as needed
    private val firebaseManager: FirebaseAuthManager
) : BaseViewModel<DomainSignUpModel, SignupIntent, SignupEffect>(
    initialState = DomainSignUpModel()
) {
    private var phoneVerificationId: String? = null

    override fun processIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.EnterFullName -> updateAndValidate { it.copy(fullName = intent.name) }
            is SignupIntent.EnterEmail -> updateAndValidate { it.copy(email = intent.email) }
            is SignupIntent.EnterPassword -> updateAndValidate { it.copy(password = intent.password) }
            is SignupIntent.EnterConfirmPassword -> updateAndValidate { it.copy(confirmPassword = intent.password) }
            is SignupIntent.UploadProfilePhoto -> updateAndValidate { it.copy(profilePhotoPath = intent.path) }
            is SignupIntent.UploadIdDocument -> updateAndValidate { it.copy(idDocumentPath = intent.path) }

            is SignupIntent.EnterPhone -> {
                requestPhoneCode(activity = intent.activity, phone = intent.phone)
            }

            is SignupIntent.SendResetCode -> {
                sendResetCode(intent.activity)
            }

            is SignupIntent.VerifyCode -> {
                verifyCode(intent.code)
            }

            SignupIntent.NextStep -> {
                goToNextStep()
            }

            SignupIntent.PreviousStep -> {
                goToPreviousStep()
            }
        }
    }

    private fun goToNextStep() {
        if (!state.value.canContinue) {
            sendEffect(SignupEffect.ShowError("Complete all required fields."))
            return
        }

        val next = when (state.value.currentStep) {
            SignupStep.PersonalInfo -> SignupStep.Contact
            SignupStep.Contact -> SignupStep.Security
            SignupStep.Security -> SignupStep.Documents
            SignupStep.Documents -> SignupStep.Verification
            SignupStep.Verification -> SignupStep.Success
            SignupStep.Success -> SignupStep.Success
        }

        updateAndValidate { it.copy(currentStep = next) }

        if (next == SignupStep.Success) {
            sendEffect(SignupEffect.RegisterSuccess(state.value))
        }
    }

    private fun goToPreviousStep() {
        val prev = when (state.value.currentStep) {
            SignupStep.Contact -> SignupStep.PersonalInfo
            SignupStep.Security -> SignupStep.Contact
            SignupStep.Documents -> SignupStep.Security
            SignupStep.Verification -> SignupStep.Documents
            SignupStep.PersonalInfo, SignupStep.Success -> SignupStep.PersonalInfo
        }

        updateState { it.copy(currentStep = prev) }
    }

    private fun requestPhoneCode(activity: Any?, phone: String) {
        viewModelScope.launch {
            updateAndValidate { it.copy(phone = phone) }

            try {
                val digits = phone.filter { it.isDigit() }
                val result = firebaseManager.requestPhoneCode(digits, activity)
                if (result.isSuccess) {
                    phoneVerificationId = result.getOrNull()
                    // advance to verification screen
                    goToNextStep()
                } else {
                    sendEffect(SignupEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to send SMS"))
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Unknown error"))
            }
        }
    }

    private fun sendResetCode(activity: Any?) {
        viewModelScope.launch {
            try {
                val digits = state.value.phone.filter { it.isDigit() }
                val result = firebaseManager.requestPhoneCode(digits, activity)
                if (result.isSuccess) {
                    phoneVerificationId = result.getOrNull()
                    sendEffect(SignupEffect.ShowToast("SMS reenviado"))
                } else {
                    sendEffect(SignupEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to send SMS"))
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Unknown error"))
            }
        }
    }

    private fun verifyCode(code: String) {
        viewModelScope.launch {
            val id = phoneVerificationId
            if (id == null) {
                sendEffect(SignupEffect.ShowError("Verification id missing"))
                return@launch
            }

            try {
                val result = firebaseManager.verifyPhoneCode(id, code)
                if (result.isSuccess) {
                    updateState { it.copy(canContinue = true) }
                    goToNextStep()
                } else {
                    sendEffect(SignupEffect.ShowError(result.exceptionOrNull()?.message ?: "Código inválido"))
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Unknown error"))
            }
        }
    }

    private fun validate(state: DomainSignUpModel): Boolean {
        return when (state.currentStep) {

            SignupStep.PersonalInfo ->
                state.fullName.isNotBlank()

            SignupStep.Contact ->
                state.email.isNotBlank() && state.phone.isNotBlank()

            SignupStep.Security ->
                state.password.length >= 6 &&
                        state.confirmPassword == state.password

            SignupStep.Documents ->
                state.profilePhotoPath != null &&
                        state.idDocumentPath != null

            SignupStep.Verification ->
                false // Aqui o NEXT não aparece, só o botão de "Verify"

            SignupStep.Success ->
                true
        }
    }

    private fun updateAndValidate(transform: (DomainSignUpModel) -> DomainSignUpModel) {
        val newState = transform(state.value)
        updateState {
            newState.copy(
                canContinue = validate(newState)
            )
        }
    }
}
