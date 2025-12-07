package com.triappdriver.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.triappdriver.local.entity.RideEntity
import com.triappdriver.presentation.ViewState
import com.triappdriver.presentation.feature.profile.ProfileStep

data class ProfileDomainModel(
    val currentStep: ProfileStep = ProfileStep.Main,
    // Perfil
    val name: String = "João Santos",
    val email: String = "joao.santos@email.com",
    val phone: String = "+55 11 98765-4321",

    // Carteira
    val walletBalance: String = "R$ 45.00",
    val walletList: List<WalletDomainModel> = mockWallet,

    // Histórico (Stats)
    val historyTotalRides: String = "5",
    val historyTotalSpent: String = "R$ 108.90",
    val historyRating: String = "★ 4.8",

    // Lista de Histórico
    val rideHistory: List<RideHistoryDomainModel> = mockHistory
) : ViewState<ProfileDomainModel>

data class RideHistoryDomainModel(
    val rideId: String,
    val date: String,
    val origin: String,
    val destination: String,
    val riderName: String,
    val price: String,
    val paymentMethod: String,
    val distanceTime: String
)

data class WalletDomainModel(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val subtitle: String,
    val extraInfo: String?,
    val isDefault: Boolean = false,
    val isSelected: Boolean = false,
    val cardOption: CardOption = CardOption.ANOTHER
) {
    enum class CardOption {
        CREATE,
        UPDATED,
        DELETE,
        ANOTHER
    }
}

val mockWallet = listOf<WalletDomainModel>(
    WalletDomainModel(
        icon = Icons.Default.CreditCard,
        color = Color(0xFF4B89FF),
        title = "Visa",
        subtitle = "•••• 4242",
        extraInfo = "Crédito",
        isDefault = true
    ),

    WalletDomainModel(
        icon = Icons.Default.CreditCard,
        color = Color(0xFFA855F7),
        title = "Mastercard",
        subtitle = "•••• 8888",
        extraInfo = "Débito",
        isSelected = true
    ),
    WalletDomainModel(
        icon = Icons.Default.QrCode,
        color = Color(0xFF2DD4BF),
        title = "PIX",
        subtitle = "PIX",
        extraInfo = null,
        isSelected = true
    ),
    WalletDomainModel(
        icon = Icons.Default.AttachMoney,
        color = Color(0xFF22C55E),
        title = "Dinheiro",
        subtitle = "Dinheiro",
        extraInfo = null,
        isSelected = true
    )
)

val mockHistory = listOf(
    RideHistoryDomainModel(
        rideId = "fdsfsd",
        date = "06 Nov 2025 • 14:30",
        origin = "Rua Augusta, 2000",
        destination = "Shopping Iguatemi",
        riderName = "Carlos Silva",
        price = "R$ 15.90",
        paymentMethod = "Visa •••• 4242",
        distanceTime = "4.2 km • 12 min"
    ),
    RideHistoryDomainModel(
        rideId = "fdsffdsafsdafasd",
        date = "05 Nov 2025 • 09:15",
        origin = "Av. Paulista, 1578",
        destination = "Aeroporto de Congonhas",
        riderName = "Ana Souza",
        price = "R$ 32.50",
        paymentMethod = "Mastercard •••• 8890",
        distanceTime = "8.5 km • 25 min"
    ),
    RideHistoryDomainModel(
        rideId = "fdsffdsfdsafdssd",
        date = "01 Nov 2025 • 20:00",
        origin = "Rua Funchal, 100",
        destination = "Parque Ibirapuera",
        riderName = "Roberto Costa",
        price = "R$ 12.00",
        paymentMethod = "Dinheiro",
        distanceTime = "3.0 km • 10 min"
    )
)

fun RideEntity.toDomain(): RideHistoryDomainModel {
    return RideHistoryDomainModel(
        rideId = this.rideId,
        date = this.date,
        origin = this.origin,
        destination = this.destination,
        riderName = this.riderName,
        price = this.price,
        paymentMethod = this.paymentMethod,
        distanceTime = this.distanceTime
    )
}

fun RideHistoryDomainModel.toEntity(): RideEntity {
    return RideEntity(
        rideId = this.rideId,
        date = this.date,
        origin = this.origin,
        destination = this.destination,
        riderName = this.riderName,
        price = this.price,
        paymentMethod = this.paymentMethod,
        distanceTime = this.distanceTime
    )
}