package com.triappdriver.presentation.feature.signup

import com.triappdriver.domain.model.SignUpDomainModel
import com.triappdriver.presentation.SideEffect
import com.triappdriver.presentation.ViewIntent

sealed class SignupEffect : SideEffect<Nothing> {
    data class RegisterSuccess(val user: SignUpDomainModel) : SignupEffect()
    data class ShowError(val message: String) : SignupEffect()
    data class ShowToast(val message: String) : SignupEffect()
}

sealed class SignupIntent : ViewIntent<Nothing> {
    data class EnterFullName(val name: String) : SignupIntent()
    data class EnterPhone(val activity: Any?, val phone: String) : SignupIntent()
    data class EnterPlaca(val placa: String) : SignupIntent()
    data class EnterInsurance(val insurance: String) : SignupIntent()
    data class EnterValidate(val date: String) : SignupIntent()
    data class EnterBankingInfo(
        val selectedBank : String,
        val selectedAccountType: String,
        val agencia: String,
        val conta: String,
        val digito : String,
        val nomeTitular: String,
        val cpfCnpj: String
    ) : SignupIntent()
    data class VerifyCode(val code: String) : SignupIntent()
    data class SendResetCode(val activity: Any?) : SignupIntent()
    data class UploadProfilePhoto(val path: String) : SignupIntent()
    data class UploadIdDocument(val path: String) : SignupIntent()
    object NextStep : SignupIntent()
    object PreviousStep : SignupIntent()
}

enum class SignupStep(val title: String, val progress: Float) {
    PersonalInfo("Personal Information", 0.13f),
    Car("Car", 0.25f),
    Documents("Document verification", 0.40f),
    Banking("Banking", 0.55f),
    Contact("Contact details", 0.70f),
    Verification("Verificação SMS", 0.85f),
    Success("All set!", 1.0f)
}