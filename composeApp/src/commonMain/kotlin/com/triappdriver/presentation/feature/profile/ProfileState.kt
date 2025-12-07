package com.triappdriver.presentation.feature.profile

import com.triappdriver.domain.model.WalletDomainModel
import com.triappdriver.presentation.SideEffect
import com.triappdriver.presentation.ViewIntent


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