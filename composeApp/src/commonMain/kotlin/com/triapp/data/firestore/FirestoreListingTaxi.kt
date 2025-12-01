package com.triapp.data.firestore

import com.triapp.data.dtos.RideDriverDTO
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile

data class ListenResult(
    val driver: RideDriverDTO,
    val status: String
)

fun listenDocumentUntilDone(
    documentId: String,
): Flow<ListenResult> {

    val db = Firebase.firestore

    return db.collection("trip_status")
        .document(documentId)
        .snapshots                          // Flow<DocumentSnapshot>
        .map { snapshot ->
            val data: Map<String, Any?> = snapshot.data() ?: emptyMap()
            val status = data["status"]?.toString() ?: "unknown"
            val driver = snapshot.get("driver", RideDriverDTO.serializer())

            ListenResult(driver, status)
        }
        .takeWhile { it.status != "COMPLETED" }
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