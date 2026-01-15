package com.triappdriver.domain.model

import com.triappdriver.data.dtos.LocationFirebaseDTO
import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RideRiderDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.dtos.fromMap
import com.triappdriver.domain.model.fromMap
import kotlinx.serialization.Serializable

sealed class TaxiState {

    object Idle : TaxiState()

    data class Online(
        val rider: RiderFirebaseDTO
    ) : TaxiState()

    /**
     * 🔥 SNAPSHOT COMPLETO DA CORRIDA
     */
    data class Update(
        val snapshot: RideSnapshotDTO
    ) : TaxiState()

    data class Error(
        val message: String
    ) : TaxiState()
}


@Serializable
data class RideSnapshotDTO(
    val tripId: String,
    val status: String,

    val rider: RideRiderDTO,
    val driver: RideDriverDTO? = null,

    val pickup: LocationFirebaseDTO,
    val dropoff: LocationFirebaseDTO,

    val fare: String? = null,
    val etaMin: String? = null,
    val distance: String? = null
)

fun RideSnapshotDTO.Companion.fromMap(map: Map<String, Any>): RideSnapshotDTO {
    return RideSnapshotDTO(
        tripId = map["tripId"] as String,
        status = map["status"] as String,

        rider = RideRiderDTO.fromMap(map["rider"] as Map<String, Any>),
        driver = (map["driver"] as? Map<String, Any>)?.let {
            RideDriverDTO.fromMap(it)
        },

        pickup = LocationFirebaseDTO.fromMap(
            map["pickup"] as Map<String, Any>
        ),

        dropoff = LocationFirebaseDTO.fromMap(
            map["dropoff"] as Map<String, Any>
        ),

        fare = map["fare"] as? String,
        etaMin = map["etaMin"] as? String,
        distance = map["distance"] as? String
    )
}