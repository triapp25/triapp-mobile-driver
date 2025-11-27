package com.triapp.presentation.feature.profile

import com.triapp.domain.model.WalletDomainModel
import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent


sealed class ProfileEffect : SideEffect<Nothing> {
    object LoggedOut : ProfileEffect()
    data class ShowToast(val message: String) : ProfileEffect()
}

sealed class ProfileIntent : ViewIntent<Nothing> {
    data class Navigate(val step: ProfileStep) : ProfileIntent()
    data class CreateCard(val walletDomainModel: WalletDomainModel) : ProfileIntent()
    data class UpdateCard(val walletDomainModel: WalletDomainModel) : ProfileIntent()
    data class DeleteCard(val walletDomainModel: WalletDomainModel) : ProfileIntent()
    object Back : ProfileIntent()
    object Logout : ProfileIntent()
}

enum class ProfileStep(val title: String) {
    Main("Perfil"),
    Wallet("Carteira"),
    History("Histórico")
}