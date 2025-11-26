package com.triapp.domain.model

import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.profile.ProfileStep

data class ProfileDomainModel(
    val currentStep: ProfileStep = ProfileStep.Main,
    val name: String = "João Santos",
    val email: String = "joao.santos@email.com",
    val phone: String = "+55 11 98765-4321"
) : ViewState<ProfileDomainModel>