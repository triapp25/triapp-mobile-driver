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

@Serializable
data class RiderFirebaseDTO(
    val acceptedAt: String? = null, // Usando String se a conversão de data/hora for complexa no KMP/Firestore
    val createdAt: String? = null,
    val status: String,
    val tripId: String,
    val pickup: LocationFirebaseDTO, // Supondo uma classe LocationDTO
    val rider: RiderInfoFirebaseDTO, // Supondo uma classe RiderInfoDTO
    val name: String? = null,
    val rating: Double? = null,
    val riderId: String? = null,
)

@Serializable
data class LocationFirebaseDTO(
    val lat: Double,
    val lng: Double,
    val name: String? = null // 'name' existe dentro de 'rider.location' e 'pickup' não tem 'name' na imagem
)

@Serializable
data class RiderInfoFirebaseDTO(
    val location: LocationFirebaseDTO,
    val rating: Double,
    val riderId: String
    // Note que o campo "location" dentro de "rider" tem o nome e as coordenadas
)