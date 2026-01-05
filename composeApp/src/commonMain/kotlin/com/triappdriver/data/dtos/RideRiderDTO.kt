package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RideRiderDTO(
    val id: String? = null,
    val name: String? = null,
    val note: String? = null,
    val rating: String? = null,
    val photoUrl: String? = null,
    val pickupLocation: LocationFirebaseDTO? = null,
    val dropoffLocation: LocationFirebaseDTO? = null

){
    companion object {
        fun fromMap(map: Map<String, Any>): RideRiderDTO =
            RideRiderDTO(
                id = map["driverId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                note = map["note"] as? String ?: "",
                rating = map["rating"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String ?: "",
                pickupLocation = (map["pickupLocation"] as? Map<String, Any>)?.let {
                    LocationFirebaseDTO(
                        lat = it["lat"] as? Double ?: 0.0,
                        lng = it["lng"] as? Double ?: 0.0
                    )
                },
                dropoffLocation = (map["dropoffLocation"] as? Map<String, Any>)?.let {
                    LocationFirebaseDTO(
                        lat = it["lat"] as? Double ?: 0.0,
                        lng = it["lng"] as? Double ?: 0.0
                    )
                }
            )
    }
}