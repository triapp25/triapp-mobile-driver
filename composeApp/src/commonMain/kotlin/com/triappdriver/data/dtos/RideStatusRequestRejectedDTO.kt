package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideStatusRequestRejectedDTO(
    val tripId: String,
    val driverId: String,
    val status: String,
    val reason: String,
)