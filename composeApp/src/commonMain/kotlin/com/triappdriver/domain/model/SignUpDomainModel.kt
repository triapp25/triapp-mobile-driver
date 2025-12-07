package com.triappdriver.domain.model

import com.triappdriver.presentation.ViewState
import com.triappdriver.presentation.feature.signup.SignupStep
import kotlinx.serialization.Serializable

@Serializable
data class SignUpDomainModel(
    val currentStep: SignupStep = SignupStep.PersonalInfo,
    val fullName: String = "",
    val phone: String = "",
    val placa: String = "",
    val insurance: String = "",
    val validate: String = "",
    val selectedBank : String = "",
    val selectedAccountType: String = "",
    val agencia: String = "",
    val conta: String = "",
    val digito : String = "",
    val nomeTitular: String = "",
    val cpfCnpj: String = "",
    val profilePhotoPath: String? = null,
    val idDocumentPath: String? = null,
    val isCompleted: Boolean = false,
    val canContinue: Boolean = false
): ViewState<SignUpDomainModel>