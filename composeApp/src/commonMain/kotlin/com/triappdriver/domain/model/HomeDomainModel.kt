package com.triappdriver.domain.model

import androidx.compose.runtime.Immutable
import com.triappdriver.presentation.ViewState
import dev.icerock.moko.geo.LatLng
import kotlinx.serialization.Serializable

// No arquivo HomeDomainModel.kt
@Immutable
data class HomeDomainModel(
    val step: HomeStep = HomeStep.Offline,
    // Estado do Switch
    val isOnline: Boolean = false,
    // Dados para o Dashboard (Mockado conforme Print 3)
    val earningsToday: String = "R$ 247.50",
    val ridesCount: Int = 14,
    val onlineHours: String = "6.2h",
    val rating: String = "4.9",
    // Mapa
    val routePolyline: List<Coordinate> = emptyList(),
    val driverPosition: Coordinate? = null,
    val destination: String? = null,
    val routeProgress: Float = 0f,
    val currentRating: String? = null,
    val currentRiderName: String? = null,
    val currentDestinationAddress: String? = null,
    val currentPickupAddress: String? = null,
    val currentPassengerNote: String? = null, // NOVO: para a nota
    val currentFare: String = "R$ 0,00",     // NOVO: para o valor da corrida
    val pickupCoordinate: Coordinate? = null, // NOVO: Coordenadas de pickup
    val destinationCoordinate: Coordinate? = null, // NOVO: Coordenadas de destino
    val targetCoordinate: Coordinate? = null, // NOVO: Coordenadas de destino
    // Lógica de tempo e distância (Calculados pelo ViewModel/UseCase)
    val etaMinutes: Int = 0, // Minutos restantes
    val distanceMeters: Int = 0, // Distância restante em metros
) : ViewState<HomeDomainModel>

// ... (HomeStep e suas subclasses permanecem iguais)

sealed class HomeStep {
    object Offline : HomeStep()
    object OnlineSearching : HomeStep()

    data class RideOffer(
        val passengerName: String,
        val pickupAddress: String,
        val destinationAddress: String,
        val distanceToPickup: String,
        val estimatedFare: String,
        val eta: String,
        val passengerNote: String?
    ) : HomeStep()

    data class NavigatingToPickup(
        val passengerName: String,
        val pickupAddress: String
    ) : HomeStep()

    data class InProgress(
        val passengerName: String,
        val destinationAddress: String,
        val timeRemainingMinutes: Int
    ) : HomeStep()

    data class NearingDestination(
        val passengerName: String,
        val destinationAddress: String,
        val timeRemainingMinutes: Int,
        val hasNewRideOffer: Boolean
    ) : HomeStep()

    data class RideCompleted(
        val passengerName: String
    ) : HomeStep()
}