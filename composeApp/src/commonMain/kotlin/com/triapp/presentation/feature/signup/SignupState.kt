package com.triapp.presentation.feature.signup

import com.triapp.domain.model.SignUpDomainModel
import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent
import com.triapp.presentation.feature.login.LoginIntent

sealed class SignupEffect : SideEffect<Nothing> {
    data class RegisterSuccess(val user: SignUpDomainModel) : SignupEffect()
    data class ShowError(val message: String) : SignupEffect()
    data class ShowToast(val message: String) : SignupEffect()
}



sealed class SignupIntent : ViewIntent<Nothing> {
    data class EnterFullName(val name: String) : SignupIntent()
    data class EnterEmail(val email: String) : SignupIntent()
    data class EnterPhone(val activity: Any?, val phone: String) : SignupIntent()
    data class VerifyCode(val code: String) : SignupIntent()
    data class SendResetCode(val activity: Any?) : SignupIntent()
    data class EnterPassword(val password: String) : SignupIntent()
    data class EnterConfirmPassword(val password: String) : SignupIntent()
    data class UploadProfilePhoto(val path: String) : SignupIntent()
    data class UploadIdDocument(val path: String) : SignupIntent()
    object NextStep : SignupIntent()
    object PreviousStep : SignupIntent()
}

enum class SignupStep(val title: String, val progress: Float) {
    PersonalInfo("Personal Information", 0.16f),
    Contact("Contact details", 0.33f),
    Security("Secure your account", 0.5f),
    Documents("Document verification", 0.66f),
    Verification("Verificação SMS", 0.83f),
    Success("All set!", 1.0f)
}