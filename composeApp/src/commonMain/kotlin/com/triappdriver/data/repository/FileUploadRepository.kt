package com.triappdriver.data.repository

import io.github.vinceglb.filekit.core.PlatformFile

interface FileUploadRepository {
    suspend fun uploadImage(
        file: PlatformFile,
        remotePath: String
    ): Result<String> // retorna downloadUrl
}