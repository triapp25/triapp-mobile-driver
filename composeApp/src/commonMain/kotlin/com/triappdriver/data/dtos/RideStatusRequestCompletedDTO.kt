package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideStatusRequestCompletedDTO(
    val tripId: String,
    val driverId: String,
    val distanceKm: Double,
    val durationMin: Double,
    val dropoffLocation: LocationDTO
)