package com.triapp.data.dtos

import com.triapp.domain.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class StartTaxiBody(
    val latLngInit: LatLng,
    val latLngEnd: LatLng,
)