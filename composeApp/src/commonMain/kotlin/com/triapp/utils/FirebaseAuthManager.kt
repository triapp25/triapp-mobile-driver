package com.triapp.utils

interface FirebaseAuthManager {
    suspend fun loginWithEmail(email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun verifyResetCode(code: String): Result<Unit>
    suspend fun signOut(): Result<Unit>

    // Phone Auth
    suspend fun requestPhoneCode(phone: String, activity: Any): Result<String>
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<Unit>
}