package com.triappdriver.domain.model

import com.triappdriver.presentation.BaseState
import com.triappdriver.presentation.ViewState
import com.triappdriver.presentation.feature.login.LoginStep

data class LoginDomainModel(
    val step: LoginStep = LoginStep.Login,
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showForgotPasswordModal: Boolean = false
): ViewState<LoginDomainModel>
