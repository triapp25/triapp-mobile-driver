package com.triappdriver.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.triappdriver.data.dtos.LocationFirebaseDTO
import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RideRiderDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.dtos.RiderInfoFirebaseDTO
import com.triappdriver.data.dtos.fromMap
import com.triappdriver.data.firestore.FirestoreRepository
import com.triappdriver.data.firestore.ListenResult
import com.triappdriver.domain.model.RideSnapshotDTO
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class AndroidFirestoreRepository : FirestoreRepository {

    private val db = FirebaseFirestore.getInstance("triapp-dev-nosql")

    override fun listenDocumentUntilDone(
        documentId: String
    ): Flow<ListenResult> {

        return db.collection("trip_status")
            .document(documentId)
            .snapshots()
            .map { snapshot ->

                if (!snapshot.exists()) {
                    throw IllegalStateException(
                        "Documento de status da viagem não encontrado (ID: $documentId)"
                    )
                }

                val data = snapshot.data ?: emptyMap()

                val tripId = snapshot.id
                val status = data["status"] as? String ?: "UNKNOWN"

                val riderMap = data["rider"] as? Map<String, Any>
                    ?: error("Campo rider ausente")

                val driverMap = data["driver"] as? Map<String, Any>

                val pickupMap = data["pickup"] as? Map<String, Any>
                    ?: error("Campo pickup ausente")

                val dropoffMap = data["dropoff"] as? Map<String, Any>
                    ?: error("Campo dropoff ausente")

                val snapshotDTO = RideSnapshotDTO(
                    tripId = tripId,
                    status = status,

                    rider = RideRiderDTO.fromMap(riderMap),
                    driver = driverMap?.let { RideDriverDTO.fromMap(it) },

                    pickup = LocationFirebaseDTO.fromMap(pickupMap),
                    dropoff = LocationFirebaseDTO.fromMap(dropoffMap),

                    fare = data["finalPrice"] as? String,
                    etaMin = (data["etaMinutes"] as? Double)?.toString(),
                    distance = (data["distanceEstimated"] as? Double)?.toString()
                )

                ListenResult(snapshotDTO)
            }
    }

    override fun listenOnline(
        authManager: FirebaseAuthManager
    ): Flow<RiderFirebaseDTO?> {

        val collectionRef = db.collection("drivers")
            .document(authManager.getCurrentUser()?.userId.orEmpty())
            .collection("ride-requests")

        return collectionRef.snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            mapDocumentToRideRequest(doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .firstOrNull()
            }
    }
}



fun mapDocumentToRideRequest(data: Map<String, Any?>): RiderFirebaseDTO? {
    // Tente pegar os valores do mapa, fazendo casts seguros
    val status = data["status"] as? String ?: return null
    val tripId = data["tripId"] as? String ?: return null
    val riderName = data["riderName"] as? String ?: return null
    val etaMin = data["etaMinutes"] as? Double ?: 0.0 // Valor padrão se não existir
    val distance = data["distanceEstimated"] as? Double ?: 0.0
    val fare = data["finalPrice"] as? String ?: "23.50"
    val rating = data["riderRating"] as? Double ?: return null

    // Mapear 'pickup'
    val pickupMap = data["pickup"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val pickupLat = pickupMap["lat"] as? Double ?: return null
    val pickupLng = pickupMap["lng"] as? Double ?: return null
    val pickupName = pickupMap["address"] as? String ?: return null
    val pickupLocation = LocationFirebaseDTO(lat = pickupLat, lng = pickupLng, name = pickupName)

    // Mapear 'rider'
    val riderMap = data["rider"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val riderId = riderMap["riderId"] as? String ?: ""
    val riderNote = riderMap["note"] as? String ?: ""

    // Mapear 'rider.location'
    val riderLocationMap = data["dropoff"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val riderLocationLat = riderLocationMap["lat"] as? Double ?: 0.0
    val riderLocationLng = riderLocationMap["lng"] as? Double ?: 0.0
    val riderLocationName = riderLocationMap["address"] as? String ?: ""

    val riderLocation = LocationFirebaseDTO(
        lat = riderLocationLat,
        lng = riderLocationLng,
        name = riderLocationName
    )

    val riderInfo = RiderInfoFirebaseDTO(
        location = riderLocation,
        rating = rating,
        riderId = riderId,
        name = riderName,
        note = riderNote
    )

    return RiderFirebaseDTO(
        status = status,
        tripId = tripId,
        etaMin = etaMin.toString(),
        distance = distance.toString(),
        fare = fare,
        name = riderName,
        rating = rating,
        riderId = riderId,
        pickup = pickupLocation,
        rider = riderInfo
    )
}