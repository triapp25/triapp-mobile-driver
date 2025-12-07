package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideCancelRequestDTO(
    val tripId: String,
    val reason: String
)