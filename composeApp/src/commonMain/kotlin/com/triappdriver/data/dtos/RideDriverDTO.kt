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
){
    companion object {
        fun fromMap(map: Map<String, Any>): RideDriverDTO =
            RideDriverDTO(
                id = map["driverId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                carModel = map["model"] as? String ?: "",
                rating = map["rating"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String ?: "",
                plate = map["plate"] as? String ?: "",
            )
    }
}

@Serializable
data class RiderFirebaseDTO(
    val status: String,
    val tripId: String,
    val pickup: LocationFirebaseDTO, // Supondo uma classe LocationDTO
    val rider: RiderInfoFirebaseDTO, // Supondo uma classe RiderInfoDTO
    val name: String? = null,
    val rating: Double? = null,
    val riderId: String? = null,
    val etaMin: String? = null,
    val distance: String? = null,
    val fare: String? = null,
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
    val riderId: String,
    val name: String? = null, // NOVO CAMPO: Nome do passageiro
    val note: String? = null // NOVO CAMPO: Nota/instrução de embarque
)