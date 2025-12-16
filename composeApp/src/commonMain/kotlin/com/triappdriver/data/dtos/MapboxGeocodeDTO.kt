package com.triappdriver.data.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MapboxGeocodeDTO(
    val features: List<MapboxFeature>? = null
)

@Serializable
data class MapboxFeature(
    @SerialName("place_name") val placeName: String? = null,
    val geometry: Geometry
)

@Serializable
data class Geometry(
    val coordinates: List<Double>
)

@Serializable
data class MapboxDirectionsDTO(
    val routes: List<Route>
)
@Serializable
data class Route(
    val geometry: String,
    val duration: Double,
    val distance: Double,
)