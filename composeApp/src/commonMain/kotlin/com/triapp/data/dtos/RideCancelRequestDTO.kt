package com.triapp.data.dtos

import com.triapp.domain.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class RideCancelRequestDTO(
    val tripId: String,
    val reason: String
)