package com.triappdriver.data.firestore

import com.triappdriver.data.dtos.RideDriverDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.flow.Flow

data class ListenResult(
    val driver: RideDriverDTO?,
    val status: String
)

fun listenDocumentUntilDone(
    firestoreRepository: FirestoreRepository,
    documentId: String
): Flow<ListenResult> {
    return firestoreRepository.listenDocumentUntilDone(documentId)
}

fun listenOnline(
    firestoreRepository: FirestoreRepository,
    authManager: FirebaseAuthManager
): Flow<RiderFirebaseDTO?> {
    return firestoreRepository.listenOnline(authManager)
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