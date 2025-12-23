package com.triappdriver.data.firestore

import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.utils.FirebaseAuthManager
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    fun listenDocumentUntilDone(documentId: String): Flow<ListenResult>
    fun listenOnline(authManager: FirebaseAuthManager): Flow<RiderFirebaseDTO?>
}