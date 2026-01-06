package com.triappdriver.presentation.feature.signup

import androidx.lifecycle.viewModelScope
import com.triappdriver.data.repository.FileUploadRepository
import com.triappdriver.data.repository.ProfileRepository
import com.triappdriver.domain.model.UploadStatus
import com.triappdriver.domain.model.SignUpDomainModel as DomainSignUpModel
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.FirebaseAuthManager
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.launch

class SignupViewModel(
    private val uploadRepository: FileUploadRepository,
    private val repository: ProfileRepository,
    private val firebaseManager: FirebaseAuthManager
) : BaseViewModel<DomainSignUpModel, SignupIntent, SignupEffect>(
    initialState = DomainSignUpModel()
) {
    private var phoneVerificationId: String? = null

    override fun processIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.EnterFullName -> updateAndValidate { it.copy(fullName = intent.name) }
            is SignupIntent.EnterInsurance -> updateAndValidate { it.copy(insurance = intent.insurance) }
            is SignupIntent.EnterPlaca -> updateAndValidate { it.copy(placa = intent.placa) }
            is SignupIntent.EnterValidate -> updateAndValidate { it.copy(validate = intent.date) }
            is SignupIntent.UploadProfilePhoto -> {
                updateState {
                    it.copy(profileUploadStatus = UploadStatus.UPLOADING)
                }

                uploadFile(
                    file = intent.file,
                    onSuccess = { url ->
                        updateAndValidate {
                            it.copy(
                                profilePhotoUrl = url,
                                profileUploadStatus = UploadStatus.SUCCESS
                            )
                        }
                    },
                    onError = {
                        updateState {
                            it.copy(profileUploadStatus = UploadStatus.ERROR)
                        }
                        sendEffect(SignupEffect.ShowError("Erro ao enviar foto de perfil"))
                    }
                )
            }

            is SignupIntent.UploadIdDocument -> {
                updateState {
                    it.copy(documentUploadStatus = UploadStatus.UPLOADING)
                }

                uploadFile(
                    file = intent.file,
                    onSuccess = { url ->
                        updateAndValidate {
                            it.copy(
                                idDocumentUrl = url,
                                documentUploadStatus = UploadStatus.SUCCESS
                            )
                        }
                    },
                    onError = {
                        updateState {
                            it.copy(documentUploadStatus = UploadStatus.ERROR)
                        }
                        sendEffect(SignupEffect.ShowError("Erro ao enviar documento"))
                    }
                )
            }

            is SignupIntent.EnterPhone -> {
                requestPhoneCode(activity = intent.activity, phone = intent.phone)
            }

            is SignupIntent.SendResetCode -> {
                sendResetCode(intent.activity)
            }

            is SignupIntent.VerifyCode -> {
                verifyCode(intent.code)
            }

            SignupIntent.NextStep -> {
                goToNextStep()
            }

            SignupIntent.PreviousStep -> {
                goToPreviousStep()
            }
        }
    }

    private fun createUser(next: SignupStep) {
        viewModelScope.launch {
            runCatching {
                repository.createUser(state.value)
            }.onSuccess {
                updateState { it.copy(isCompleted = true, currentStep = next) }
                sendEffect(SignupEffect.RegisterSuccess(state.value))
            }.onFailure {
                sendEffect(SignupEffect.ShowError("Erro ao criar motorista: ${it.message}"))
                return@launch
            }
        }
    }

    private fun uploadFile(
        file: PlatformFile,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = uploadRepository.uploadImage(
                    file = file,
                    remotePath = "drivers/temp/${file.name}"
                )

                result
                    .onSuccess(onSuccess)
                    .onFailure { onError() }

            } catch (e: Exception) {
                onError()
            }
        }
    }

    private fun goToNextStep() {
        if (!state.value.canContinue) {
            sendEffect(SignupEffect.ShowError("Complete os campos obrigatórios"))
            return
        }

        val next = when (state.value.currentStep) {
            SignupStep.PersonalInfo -> SignupStep.Car
            SignupStep.Car -> SignupStep.Documents
            SignupStep.Documents -> SignupStep.Contact
            SignupStep.Contact -> SignupStep.Verification
            SignupStep.Verification -> SignupStep.Success
            SignupStep.Success -> SignupStep.Success
        }

        if (next == SignupStep.Success) {
            createUser(next)
        } else {
            updateAndValidate { it.copy(currentStep = next) }
        }

    }

    private fun goToPreviousStep() {
        val prev = when (state.value.currentStep) {
            SignupStep.Car -> SignupStep.PersonalInfo
            SignupStep.Documents -> SignupStep.Car
            SignupStep.Contact -> SignupStep.Documents
            SignupStep.Verification -> SignupStep.Contact
            SignupStep.PersonalInfo, SignupStep.Success -> SignupStep.PersonalInfo
        }

        updateState { it.copy(currentStep = prev) }
    }

    private fun requestPhoneCode(activity: Any?, phone: String) {
        viewModelScope.launch {
            updateAndValidate { it.copy(phone = phone) }

            try {
                val digits = phone.filter { it.isDigit() }
                val result = firebaseManager.requestPhoneCode(digits, activity)
                if (result.isSuccess) {
                    phoneVerificationId = result.getOrNull()
                    goToNextStep()
                } else {
                    sendEffect(
                        SignupEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Falha ao enviar SMS"
                        )
                    )
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Erro desconhecido"))
            }
        }
    }

    private fun sendResetCode(activity: Any?) {
        viewModelScope.launch {
            try {
                val digits = state.value.phone.filter { it.isDigit() }
                val result = firebaseManager.requestPhoneCode(digits, activity)
                if (result.isSuccess) {
                    phoneVerificationId = result.getOrNull()
                    sendEffect(SignupEffect.ShowToast("SMS reenviado"))
                } else {
                    sendEffect(
                        SignupEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Falha ao enviar SMS"
                        )
                    )
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Erro desconhecido"))
            }
        }
    }

    private fun verifyCode(code: String) {
        viewModelScope.launch {
            val id = phoneVerificationId
            if (id == null) {
                sendEffect(SignupEffect.ShowError("Verificação não iniciada"))
                return@launch
            }

            try {
                val result = firebaseManager.verifyPhoneCode(id, code)
                if (result.isSuccess) {
                    updateState { it.copy(canContinue = true) }
                    goToNextStep()
                } else {
                    sendEffect(
                        SignupEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Código inválido"
                        )
                    )
                }
            } catch (t: Throwable) {
                sendEffect(SignupEffect.ShowError(t.message ?: "Erro desconhecido"))
            }
        }
    }

    private fun validate(state: DomainSignUpModel): Boolean {
        return when (state.currentStep) {

            SignupStep.PersonalInfo ->
                state.fullName.isNotBlank()

            SignupStep.Contact ->
                state.phone.isNotBlank()

            SignupStep.Car ->
                state.placa.isNotBlank() && state.insurance.isNotBlank() && state.validate.isNotBlank()

            SignupStep.Documents ->
                state.profilePhotoUrl != null &&
                        state.idDocumentUrl != null

            SignupStep.Verification ->
                false // Aqui o NEXT não aparece, só o botão de "Verify"

            SignupStep.Success ->
                true

        }
    }

    private fun updateAndValidate(transform: (DomainSignUpModel) -> DomainSignUpModel) {
        val newState = transform(state.value)
        updateState {
            newState.copy(
                canContinue = validate(newState)
            )
        }
    }
}
