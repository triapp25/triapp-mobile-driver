package com.triapp.presentation.feature.login

import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent

sealed class LoginIntent : ViewIntent<Nothing> {
    data class EnterEmail(val email: String) : LoginIntent()
    data class EnterPhone(val phone: String) : LoginIntent()
    data class EnterPassword(val password: String) : LoginIntent()
    data class EnterResetCode(val code: String) : LoginIntent()
    data class SubmitLogin(val activity: Any) : LoginIntent()

    object ForgotPassword : LoginIntent()
    object SendResetCode : LoginIntent()
    object VerifyResetCode : LoginIntent()
    object BackToLogin : LoginIntent()
    object GoToSignup : LoginIntent()
    object DismissForgotPassword : LoginIntent()
}

sealed class LoginEffect : SideEffect<Nothing> {
    data class ShowSnackbar(val message: String) : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
    object ShowForgotPassword : LoginEffect()
    object HideForgotPassword : LoginEffect()
    object GoToResetCode : LoginEffect()
    object BackToLogin : LoginEffect()
    object NavigateToHome : LoginEffect()
    object NavigateToSignup : LoginEffect()

}

enum class LoginStep {
    Login,
    ResetCode
}