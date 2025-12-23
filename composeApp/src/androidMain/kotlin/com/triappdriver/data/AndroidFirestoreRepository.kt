package com.triappdriver.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.triappdriver.data.dtos.LocationFirebaseDTO
import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.dtos.RiderInfoFirebaseDTO
import com.triappdriver.data.firestore.FirestoreRepository
import com.triappdriver.data.firestore.ListenResult
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class AndroidFirestoreRepository : FirestoreRepository {
    val db = FirebaseFirestore.getInstance("triapp-dev-nosql")

    override fun listenDocumentUntilDone(
        documentId: String
    ): Flow<ListenResult> {
        return db.collection("trip_status")
            .document(documentId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) {
                    throw IllegalStateException("Documento de status da viagem não encontrado no Firestore (ID: $documentId).")
                }

                val data: Map<String, Any?> = snapshot.data ?: emptyMap()

                val status = data["status"]?.toString() ?: "UNKNOWN"

                val driver = try {
                    snapshot.get("driver")?.let { raw ->
                        val json = raw as Map<String, Any>
                        RideDriverDTO.fromMap(json)
                    }
                } catch (e: Exception) {
                    null
                }

                ListenResult(driver, status)
            }
    }

    override fun listenOnline(authManager: FirebaseAuthManager): Flow<RiderFirebaseDTO?> {
        val db = FirebaseFirestore.getInstance("triapp-dev-nosql")

        val collectionRef = db.collection("drivers")
            .document(authManager.getCurrentUser()?.userId.orEmpty())
            .collection("ride-requests")

        return collectionRef.snapshots()
            .map { snapshot ->
                // 1. Mapeia todos os documentos para RiderFirebaseDTO? (ou null em caso de erro/falha)
                val mappedRides: List<RiderFirebaseDTO?> =
                    snapshot.documents.map { documentSnapshot ->
                        try {
                            mapDocumentToRideRequest(documentSnapshot.data ?: emptyMap())
                        } catch (e: Exception) {
                            println("Erro ao mapear documento: $e")
                            null
                        }
                    }

                // 2. Encontra o primeiro que não é nulo.
                mappedRides.firstOrNull { it != null }
            }
    }
}


fun mapDocumentToRideRequest(data: Map<String, Any?>): RiderFirebaseDTO? {
    // Tente pegar os valores do mapa, fazendo casts seguros
    val status = data["status"] as? String ?: return null
    val tripId = data["tripId"] as? String ?: return null
    val riderName = data["riderName"] as? String ?: return null
    val rating = data["riderRating"] as? Double ?: return null

    // Mapear 'pickup'
    val pickupMap = data["pickup"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val pickupLat = pickupMap["lat"] as? Double ?: return null
    val pickupLng = pickupMap["lng"] as? Double ?: return null
    val pickupLocation = LocationFirebaseDTO(lat = pickupLat, lng = pickupLng)

    // Mapear 'rider'
    val riderMap = data["rider"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val riderId = riderMap["riderId"] as? String ?: ""
    val riderNote = riderMap["note"] as? String ?: ""

    // Mapear 'rider.location'
    val riderLocationMap = riderMap["location"] as? Map<String, Any?> ?: emptyMap<String, Any?>()
    val riderLocationLat = riderLocationMap["lat"] as? Double ?: 0.0
    val riderLocationLng = riderLocationMap["lng"] as? Double ?: 0.0
    val riderLocationName = riderLocationMap["name"] as? String ?: ""

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
        pickup = pickupLocation,
        rider = riderInfo
    )
}