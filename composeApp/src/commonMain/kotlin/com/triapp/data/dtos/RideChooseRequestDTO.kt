package com.triapp.data.dtos

import com.triapp.domain.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class RideChooseRequestDTO(
    val tripId: String,
    val category: String
)