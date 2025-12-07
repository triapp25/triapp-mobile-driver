package com.triappdriver.presentation.feature.login

import com.triappdriver.presentation.SideEffect
import com.triappdriver.presentation.ViewIntent

sealed class LoginIntent : ViewIntent<Nothing> {
    data class EnterEmail(val email: String) : LoginIntent()
    data class VerifyCode(val code: String) : LoginIntent()
    data class SubmitLogin(val activity: Any?, val phone: String) : LoginIntent()
    data class SendResetCode(val activity: Any?) : LoginIntent()

    object BackToLogin : LoginIntent()
    object NavigateToSignup : LoginIntent()
}

sealed class LoginEffect : SideEffect<Nothing> {
    data class ShowSnackbar(val message: String) : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
    object BackToLogin : LoginEffect()
    object NavigateToHome : LoginEffect()
    object NavigateToSignup : LoginEffect()

}

enum class LoginStep {
    Login,
    ResetCode
}