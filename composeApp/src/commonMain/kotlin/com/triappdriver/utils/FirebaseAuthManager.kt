package com.triappdriver.utils


data class AppUser(
    val userId: String,
    val displayName: String,
    val email: String
)

interface FirebaseAuthManager {
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun verifyResetCode(code: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): AppUser?
    suspend fun requestPhoneCode(phone: String, activity: Any?): Result<String>
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<Unit>
}

class FirebaseServiceImpl : FirebaseAuthManager {
    override suspend fun requestPhoneCode(
        phone: String,
        activity: Any?
    ): Result<String> {
        return requestPhoneCodeFirebase(phone, activity)
    }

    override suspend fun verifyPhoneCode(
        verificationId: String,
        code: String
    ): Result<Unit> {
        return verifyPhoneCodeFirebase(verificationId, code)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return sendPasswordResetFirebase(email)
    }

    override suspend fun verifyResetCode(code: String): Result<Unit> {
        return verifyResetCodeFirebase(code)
    }

    override suspend fun signOut() =
        signOutFirebase()

    override fun getCurrentUser(): AppUser? {
        return getCurrentUserFirebase()
    }
}

expect fun getCurrentUserFirebase(): AppUser?
expect suspend fun sendPasswordResetFirebase(email: String): Result<Unit>
expect suspend fun verifyResetCodeFirebase(code: String): Result<Unit>
expect suspend fun signOutFirebase(): Result<Unit>
expect suspend fun requestPhoneCodeFirebase(phone: String, activity: Any?): Result<String>
expect suspend fun verifyPhoneCodeFirebase(verificationId: String, code: String): Result<Unit>