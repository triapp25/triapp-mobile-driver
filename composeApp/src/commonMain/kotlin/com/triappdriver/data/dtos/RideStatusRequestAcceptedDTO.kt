package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideStatusRequestAcceptedDTO(
    val tripId: String,
    val driverId: String,
    val currentLocation: LocationDTO
)