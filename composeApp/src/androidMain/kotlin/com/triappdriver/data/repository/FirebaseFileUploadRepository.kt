package com.triappdriver.data.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.tasks.await
import kotlin.runCatching

class FirebaseFileUploadRepository(
    private val context: Context
) : FileUploadRepository {

    private val storage = Firebase.storage

    override suspend fun uploadImage(
        file: PlatformFile,
        remotePath: String
    ): Result<String> = runCatching {

        val uri = file.uri ?: error("Uri inválida")

        val ref = storage.reference.child(remotePath)

        ref.putFile(uri).await()

        ref.downloadUrl.await().toString()
    }
}
