package com.triapp.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideDTO(
    val tripId: String,
    val tripStatusDocPath: String,
    val categories: List<RideCategoriesDTO>
)

@Serializable
data class RideCategoriesDTO(
    val categoryId: String,
    val label: String,
    val priceEstimate: String,
    val etaSeconds: String,
)