package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideRequestDTO(
    val riderId: String,
    val pickup: LocationDTO,
    val dropoff: LocationDTO,
    val riderNote: String
)

@Serializable
data class LocationDTO(
    val lat: Double,
    val lng: Double,
    val address: String
)