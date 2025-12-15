package com.triappdriver.data.firestore

import com.triappdriver.data.dtos.LocationFirebaseDTO
import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.dtos.RiderInfoFirebaseDTO
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile

data class ListenResult(
    val driver: RideDriverDTO?, // Pode ser nulo se não houver driver alocado ainda
    val status: String
)

fun listenDocumentUntilDone(documentId: String): Flow<ListenResult> {
    val db = Firebase.firestore

    return db.collection("trip_status")
        .document(documentId)
        .snapshots
        .map { snapshot ->
            val data = snapshot.data<Map<String, Any?>>() ?: emptyMap()
            val status = data["status"]?.toString() ?: "UNKNOWN"

            // Tenta pegar o objeto driver, se existir
            val driver = try {
                snapshot.get("driver", RideDriverDTO.serializer())
            } catch (e: Exception) {
                null
            }

            ListenResult(driver, status)
        }
        .takeWhile {  it.status != "COMPLETED"  }
}

fun listenOnline(): Flow<RiderFirebaseDTO> {
    val db = Firebase.firestore
    val auth = Firebase.auth

    val collectionRef = db.collection("drivers")
        .document(auth.currentUser?.uid.orEmpty())
        .collection("rideRequests")

    return collectionRef.snapshots
        .map { snapshot ->
            // Mapear cada documento no CollectionSnapshot
            snapshot.documents.firstOrNull { documentSnapshot ->
                try {
                    val dataMap = documentSnapshot.data()
                    mapDocumentToRideRequest(dataMap)

                } catch (e: Exception) {
                    println("Erro ao mapear documento: $e")
                    null // Ignora documentos com erro de mapeamento
                }
            }
        }
}

fun mapDocumentToRideRequest(data: Map<String, Any?>): RiderFirebaseDTO? {
    // Tente pegar os valores do mapa, fazendo casts seguros
    val status = data["status"] as? String ?: return null
    val tripId = data["tripId"] as? String ?: return null
    val acceptedAt = data["acceptedAt"] as? String // Exemplo: 13 de dezembro de 2025 às 00:25:20 UTC
    val createdAt = data["createdAt"] as? String

    // Mapear 'pickup'
    val pickupMap = data["pickup"] as? Map<String, Any?> ?: return null
    val pickupLat = pickupMap["lat"] as? Double ?: return null
    val pickupLng = pickupMap["lng"] as? Double ?: return null
    val pickupLocation = LocationFirebaseDTO(lat = pickupLat, lng = pickupLng)

    // Mapear 'rider'
    val riderMap = data["rider"] as? Map<String, Any?> ?: return null
    val rating = riderMap["rating"] as? Double ?: 0.0
    val riderId = riderMap["riderId"] as? String ?: ""

    // Mapear 'rider.location'
    val riderLocationMap = riderMap["location"] as? Map<String, Any?> ?: return null
    val riderLocationLat = riderLocationMap["lat"] as? Double ?: return null
    val riderLocationLng = riderLocationMap["lng"] as? Double ?: return null
    val riderLocationName = riderLocationMap["name"] as? String

    val riderLocation = LocationFirebaseDTO(
        lat = riderLocationLat,
        lng = riderLocationLng,
        name = riderLocationName
    )

    val riderInfo = RiderInfoFirebaseDTO(
        location = riderLocation,
        rating = rating,
        riderId = riderId
    )

    return RiderFirebaseDTO(
        acceptedAt = acceptedAt,
        createdAt = createdAt,
        status = status,
        tripId = tripId,
        pickup = pickupLocation,
        rider = riderInfo
    )
}

enum class TripStatus {
    CREATED,
    PRICING_AVAILABLE,
    MATCHING_IN_PROGRESS,
    DRIVER_REQUESTED,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    ONGOING,
    COMPLETED
}