package com.triappdriver.data.firestore

import com.triappdriver.data.dtos.RideDriverDTO
import dev.gitlive.firebase.Firebase
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