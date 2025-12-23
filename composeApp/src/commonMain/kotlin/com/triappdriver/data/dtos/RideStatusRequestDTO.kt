package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideStatusRequestDTO(
    val tripId: String,
    val driverId: String
)