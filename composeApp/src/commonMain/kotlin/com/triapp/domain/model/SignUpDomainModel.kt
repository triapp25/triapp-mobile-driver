package com.triapp.domain.model

import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.signup.RegistrationStep

data class SignUpDomainModel(
    val currentStep: RegistrationStep = RegistrationStep.PersonalInfo,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val profilePhotoPath: String? = null,
    val idDocumentPath: String? = null,
    val isCompleted: Boolean = false
): ViewState<SignUpDomainModel>