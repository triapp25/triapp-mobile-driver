package com.triapp.presentation.feature.login

import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent

sealed class LoginIntent : ViewIntent<Nothing> {
    data class EnterEmail(val email: String) : LoginIntent()
    data class RequestPasswordReset(val password: String) : LoginIntent()
    data class VerifyCode(val code: String) : LoginIntent()
    data class SubmitLogin(val activity: Any?, val phone: String) : LoginIntent()
    data class SendResetCode(val activity: Any?) : LoginIntent()

    object OpenForgotPassword : LoginIntent()
    object BackToLogin : LoginIntent()
    object NavigateToSignup : LoginIntent()
    object DismissForgotPassword : LoginIntent()
}

sealed class LoginEffect : SideEffect<Nothing> {
    data class ShowSnackbar(val message: String) : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
    object ShowForgotPassword : LoginEffect()
    object HideForgotPassword : LoginEffect()
    object BackToLogin : LoginEffect()
    object NavigateToHome : LoginEffect()
    object NavigateToSignup : LoginEffect()

}

enum class LoginStep {
    Login,
    ResetCode
}