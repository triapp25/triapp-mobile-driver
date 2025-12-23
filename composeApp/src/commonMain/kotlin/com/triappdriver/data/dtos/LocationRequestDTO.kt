package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LocationRequest(
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val status: String
)
