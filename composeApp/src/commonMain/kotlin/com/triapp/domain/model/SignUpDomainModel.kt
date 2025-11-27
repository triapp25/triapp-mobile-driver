package com.triapp.domain.model

import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.signup.SignupStep
import kotlinx.serialization.Serializable

@Serializable
data class SignUpDomainModel(
    val currentStep: SignupStep = SignupStep.PersonalInfo,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val profilePhotoPath: String? = null,
    val idDocumentPath: String? = null,
    val isCompleted: Boolean = false,
    val canContinue: Boolean = false
): ViewState<SignUpDomainModel>