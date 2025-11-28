package com.triapp.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.home.HomeStep
import kotlinx.serialization.Serializable

@Immutable
data class HomeDomainModel(
    // Controle de Navegação
    val step: HomeStep = HomeStep.Initial,

    // --- CAMPOS DE TEXTO (Faltavam estes) ---
    val pickup: String = "",  // Texto do campo de origem
    val dropoff: String = "", // Texto do campo de destino

    // Endereços completos (Objetos)
    val pickupAddress: SearchResult? = null,
    val dropoffAddress: SearchResult? = null,

    // Busca e Resultados
    val searchResults: List<SearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,

    // Seleção de Corrida
    val availableOptions: List<RideOption> = emptyList(),
    val selectedOption: RideOption? = null,
    val isLoadingRide: Boolean = false,

    // Pagamento
    val defaultWallet: WalletDomainModel? = null,

    // --- DADOS DA CORRIDA ATIVA (Faltavam estes) ---
    val ride: RideInfo? = null,      // Dados técnicos da corrida (distância, eta)
    val driver: DriverInfo? = null,  // Dados visuais do motorista

    // Mensagens de Status
    val etaMessage: String = "",
    val tripStatusMessage: String = "",
    val routeProgress: Float = 0f,

    // --- DADOS DO MAPA (Faltavam estes) ---
    val driverPosition: LatLng? = null,            // Posição atual do ícone do carro
    val routePolyline: List<LatLng> = emptyList(), // Linha da rota no mapa

    // Erros
    val error: String? = null
) : ViewState<HomeDomainModel>

// ================== MODELOS AUXILIARES ==================

@Immutable
@Serializable
data class SearchResult(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@Immutable
data class RideOption(
    val id: String,
    val name: String,
    val time: String,
    val price: String,
    val icon: ImageVector
)

@Immutable
@Serializable
data class DriverInfo(
    val id: String,
    val name: String,
    val carModel: String,
    val plate: String,
    val rating: String,
    val photoUrl: String? = null
)

@Immutable
@Serializable
data class RideInfo(
    val driver: DriverInfo,
    val etaMinutes: Int,
    val remainingKm: Double
)

@Immutable
@Serializable
data class LatLng(
    val lat: Double,
    val lng: Double
)