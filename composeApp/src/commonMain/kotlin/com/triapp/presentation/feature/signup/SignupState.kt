package com.triapp.presentation.feature.signup

import com.triapp.domain.model.SignUpDomainModel
import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent

sealed class SignupEffect : SideEffect<Nothing> {
    data class RegisterSuccess(val user: SignUpDomainModel) : SignupEffect()
}


sealed class SignupIntent : ViewIntent<Nothing> {
    data class EnterFullName(val name: String) : SignupIntent()
    data class EnterEmail(val email: String) : SignupIntent()
    data class EnterPhone(val phone: String) : SignupIntent()
    data class EnterPassword(val password: String) : SignupIntent()
    data class EnterConfirmPassword(val password: String) : SignupIntent()
    data class UploadProfilePhoto(val path: String) : SignupIntent()
    data class UploadIdDocument(val path: String) : SignupIntent()
    object NextStep : SignupIntent()
    object PreviousStep : SignupIntent()
}

enum class RegistrationStep {
    PersonalInfo,
    ContactDetails,
    SecureAccount,
    DocumentVerification,
    AllSet
}
