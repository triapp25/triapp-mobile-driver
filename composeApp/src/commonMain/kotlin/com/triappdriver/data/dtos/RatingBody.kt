package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RatingBody(
    val tripId: String,
    val rating: Int
)