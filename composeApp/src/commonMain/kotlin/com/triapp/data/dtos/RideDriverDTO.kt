package com.triapp.data.dtos

import com.triapp.domain.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class RideDriverDTO(
    val id: String,
    val name: String,
    val carModel: String,
    val plate: String,
    val rating: String,
    val photoUrl: String? = null
)