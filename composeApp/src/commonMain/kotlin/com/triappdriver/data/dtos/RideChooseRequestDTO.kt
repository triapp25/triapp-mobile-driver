package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideChooseRequestDTO(
    val tripId: String,
    val category: String
)