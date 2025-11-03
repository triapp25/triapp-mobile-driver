package com.triapp.utils

actual interface `FirebaseAuthManager.kt` {
    actual suspend fun loginWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(Exception("Not implemented on iOS yet"))

    actual suspend fun sendPasswordReset(email: String): Result<Unit> =
        Result.failure(Exception("Not implemented on iOS yet"))

    actual suspend fun verifyResetCode(code: String): Result<Unit> =
        Result.failure(Exception("Not implemented on iOS yet"))

    actual suspend fun signOut(): Result<Unit> =
        Result.failure(Exception("Not implemented on iOS yet"))

    actual suspend fun requestPhoneCode(phone: String): Result<String> =
        Result.failure(Exception("Phone Auth not implemented on iOS yet"))

    actual suspend fun verifyPhoneCode(verificationId: String, code: String): Result<Unit> =
        Result.failure(Exception("Phone Auth not implemented on iOS yet"))
}