package com.triappdriver.utils

import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

actual fun getCurrentUserFirebase(): AppUser? {
    return FirebaseAuth.getInstance().currentUser?.toAppUser()
}

actual suspend fun sendPasswordResetFirebase(email: String): Result<Unit> =
    try {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

actual suspend fun verifyResetCodeFirebase(code: String): Result<Unit> =
    try {
        FirebaseAuth.getInstance().verifyPasswordResetCode(code).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

actual suspend fun signOutFirebase(): Result<Unit> =
    try {
        FirebaseAuth.getInstance().signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

actual suspend fun requestPhoneCodeFirebase(phone: String, activity: Any?): Result<String> =
    try {

        val verificationId = suspendCancellableCoroutine<String> { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    cont.resume("auto") {}
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    cont.resumeWithException(p0)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    cont.resume(verificationId) {}
                }
            }

            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+55$phone")
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity as ComponentActivity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        Result.success(verificationId)
    } catch (e: Exception) {
        Result.failure(e)
    }

actual suspend fun verifyPhoneCodeFirebase(verificationId: String, code: String): Result<Unit> =
    try {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        FirebaseAuth.getInstance().signInWithCredential(credential).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

private fun FirebaseUser.toAppUser(): AppUser =
    AppUser(
        userId = uid,
        displayName = displayName ?: "Unknown",
        email = email ?: "Unknown"
    )
