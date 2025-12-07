package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideDriverDTO(
    val id: String? = null,
    val name: String? = null,
    val carModel: String? = null,
    val plate: String? = null,
    val rating: String? = null,
    val photoUrl: String? = null
)