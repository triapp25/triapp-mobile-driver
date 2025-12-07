package com.triappdriver.presentation.feature.login

import androidx.lifecycle.viewModelScope
import com.triappdriver.domain.model.LoginDomainModel
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val firebaseManager: FirebaseAuthManager
) :
    BaseViewModel<LoginDomainModel, LoginIntent, LoginEffect>(
        initialState = LoginDomainModel()
    ) {

    private var phoneVerificationId: String? = null

    override fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EnterEmail -> updateState { it.copy(email = intent.email) }
            is LoginIntent.VerifyCode -> verifyCode(intent.code)
            is LoginIntent.SubmitLogin -> requestPhoneCode(intent.activity, intent.phone)
            is LoginIntent.SendResetCode -> sendResetCode(intent.activity)
            LoginIntent.BackToLogin -> backToLogin()
            LoginIntent.NavigateToSignup -> { sendEffect(LoginEffect.NavigateToSignup) }

        }
    }


    private fun requestPhoneCode(activity: Any?, phone: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val digits = phone.filter { it.isDigit() }
            val result = firebaseManager.requestPhoneCode(digits, activity)

            if (result.isSuccess) {
                phoneVerificationId = result.getOrNull()
                updateState { it.copy(phone = phone, step = LoginStep.ResetCode) }
                sendEffect(LoginEffect.ShowSnackbar("SMS enviado"))
            } else {
                sendEffect(LoginEffect.ShowError("Erro ao enviar SMS"))
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private fun sendResetCode(activity: Any?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val digits = state.value.phone.filter { it.isDigit() }
            val result = firebaseManager.requestPhoneCode(digits, activity)

            if (result.isSuccess) {
                phoneVerificationId = result.getOrNull()
                sendEffect(LoginEffect.ShowSnackbar("SMS enviado"))
            } else {
                sendEffect(LoginEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to send reset link"))
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private fun verifyCode(code: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val id = phoneVerificationId ?: return@launch

            val result = firebaseManager.verifyPhoneCode(id, code)
            if (result.isSuccess) {
                sendEffect(LoginEffect.ShowSnackbar("Code verified successfully"))
                updateState { it.copy(step = LoginStep.Login) }
                sendEffect(LoginEffect.NavigateToHome)
            } else {
                sendEffect(LoginEffect.ShowError(result.exceptionOrNull()?.message ?: "Invalid code"))
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private fun backToLogin() {
        updateState {  it.copy(step = LoginStep.Login) }
        sendEffect(LoginEffect.BackToLogin)
    }
}
