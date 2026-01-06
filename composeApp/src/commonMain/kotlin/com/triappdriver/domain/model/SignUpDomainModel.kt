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
    val profilePhotoUrl: String? = null,
    val idDocumentUrl: String? = null,

    val profileUploadStatus: UploadStatus = UploadStatus.IDLE,
    val documentUploadStatus: UploadStatus = UploadStatus.IDLE,

    val isCompleted: Boolean = false,
    val canContinue: Boolean = false
): ViewState<SignUpDomainModel>


enum class UploadStatus {
    IDLE,
    UPLOADING,
    SUCCESS,
    ERROR
}
