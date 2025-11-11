package com.triapp.presentation.feature.login

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.LoginDomainModel
import com.triapp.presentation.BaseViewModel
import com.triapp.utils.FirebaseAuthManager
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
            is LoginIntent.EnterPhone -> updateState { it.copy(phone = intent.phone) }
            is LoginIntent.EnterPassword -> updateState { it.copy(password = intent.password) }
            is LoginIntent.EnterResetCode -> updateState { it.copy(resetCode = intent.code) }
            is LoginIntent.SubmitLogin -> requestPhoneCode(intent.activity)
            LoginIntent.ForgotPassword -> sendEffect(LoginEffect.ShowForgotPassword)
            LoginIntent.DismissForgotPassword -> sendEffect(LoginEffect.HideForgotPassword)
            LoginIntent.SendResetCode -> sendResetCode()
            LoginIntent.VerifyResetCode -> verifyCode()
            LoginIntent.BackToLogin -> backToLogin()
            LoginIntent.GoToSignup -> { sendEffect(LoginEffect.NavigateToSignup) }

        }
    }


    private fun requestPhoneCode(activity: Any?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val result = firebaseManager.requestPhoneCode(state.value.phone, activity)

            if (result.isSuccess) {
                phoneVerificationId = result.getOrNull()
                updateState { it.copy(step = LoginStep.ResetCode) }
                sendEffect(LoginEffect.ShowSnackbar("SMS enviado"))
            } else {
                sendEffect(LoginEffect.ShowError("Erro ao enviar SMS"))
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private fun sendResetCode() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val result = firebaseManager.sendPasswordReset(state.value.email)
            if (result.isSuccess) {
                sendEffect(LoginEffect.ShowSnackbar("Reset link sent"))
                sendEffect(LoginEffect.NavigateToHome)
            } else {
                sendEffect(LoginEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to send reset link"))
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private fun verifyCode() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val id = phoneVerificationId ?: return@launch
            val code = state.value.resetCode

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
