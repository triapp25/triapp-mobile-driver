package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideStatusRequestOnGoingDTO(
    val tripId: String,
    val driverId: String,
    val pickupLocation: LocationDTO
)