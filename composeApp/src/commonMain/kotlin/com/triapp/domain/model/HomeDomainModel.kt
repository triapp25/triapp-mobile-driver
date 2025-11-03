package com.triapp.domain.model

import androidx.compose.runtime.Immutable
import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.home.HomeStep

@Immutable
data class HomeDomainModel(
    val step: HomeStep = HomeStep.Idle,
    val pickup: String = "",
    val dropoff: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,


// map & ride related
    val availableOptions: List<RideOption> = emptyList(),
    val selectedOption: RideOption? = null,
    val ride: RideInfo? = null,


// driver position on map (lat, lng) - nullable when no ride
    val driverPosition: LatLng? = null,
    val routePolyline: List<LatLng> = emptyList()
) : ViewState<HomeDomainModel>


@Immutable
data class RideOption(val id: String, val name: String, val price: String, val eta: String)


@Immutable
data class DriverInfo(val id: String, val name: String, val car: String, val plate: String, val rating: Double)


@Immutable
data class RideInfo(
    val driver: DriverInfo,
    val etaMinutes: Int,
    val remainingKm: Double
)


@Immutable
data class LatLng(val lat: Double, val lng: Double)