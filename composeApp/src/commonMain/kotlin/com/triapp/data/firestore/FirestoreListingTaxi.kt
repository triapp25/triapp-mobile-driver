package com.triapp.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.*

data class ListenResult(
    val data: Map<String, Any?>,
    val status: String
)

fun listenDocumentUntilDone(
    documentId: String,
): Flow<ListenResult> {

    val db = Firebase.firestore

    return db.collection("taxi")
        .document(documentId)
        .snapshots                          // Flow<DocumentSnapshot>
        .map { snapshot ->
            val data: Map<String, Any?> = snapshot.data() ?: emptyMap()
            val status = data["status"]?.toString() ?: "unknown"

            ListenResult(data, status)
        }
        .takeWhile { it.status != "done" }
}
