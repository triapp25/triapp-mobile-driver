package com.triapp.utils

import android.app.Activity
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

class FirebaseAuthManagerImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthManager {

    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> =
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun verifyResetCode(code: String): Result<Unit> =
        try {
            firebaseAuth.verifyPasswordResetCode(code).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signOut(): Result<Unit> =
        try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun requestPhoneCode(phone: String, activity: Any): Result<String> =
        try {

            val verificationId = suspendCancellableCoroutine<String> { cont ->
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                        cont.resume("auto") {}
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        cont.resumeWithException(p0)
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        cont.resume(verificationId) {}
                    }
                }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
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

    override suspend fun verifyPhoneCode(verificationId: String, code: String): Result<Unit> =
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}