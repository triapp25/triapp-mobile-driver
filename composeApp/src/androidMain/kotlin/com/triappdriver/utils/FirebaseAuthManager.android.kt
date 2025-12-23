package com.triappdriver.utils

import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

actual fun getCurrentUserFirebase(): AppUser? =
    FirebaseAuth.getInstance().currentUser?.toAppUser()

actual suspend fun sendPasswordResetFirebase(email: String): Result<Unit> =
    runCatching {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
    }

actual suspend fun verifyResetCodeFirebase(code: String): Result<Unit> =
    runCatching {
        FirebaseAuth.getInstance().verifyPasswordResetCode(code)
    }

actual suspend fun signOutFirebase(): Result<Unit> =
    runCatching {
        FirebaseAuth.getInstance().signOut()
    }

actual suspend fun requestPhoneCodeFirebase(phone: String, activity: Any?): Result<String> {
    return try {
        val verificationId = suspendCancellableCoroutine<String> { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    cont.resume(id) {}
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                    cont.resume("auto") {}

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    cont.resumeWithException(p0)
                }
            }


            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber("+55$phone")
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity as ComponentActivity)
                    .setCallbacks(callbacks)
                    .build()
            )
        }

        Result.success(verificationId)
    } catch (e: Exception) {
        Result.failure(e)
    }
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
