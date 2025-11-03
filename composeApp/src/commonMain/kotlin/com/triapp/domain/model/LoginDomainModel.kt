package com.triapp.domain.model

import com.triapp.presentation.BaseState
import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.login.LoginStep

data class LoginDomainModel(
    val step: LoginStep = LoginStep.Login,
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val resetCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showForgotPasswordModal: Boolean = false
): ViewState<LoginDomainModel>
