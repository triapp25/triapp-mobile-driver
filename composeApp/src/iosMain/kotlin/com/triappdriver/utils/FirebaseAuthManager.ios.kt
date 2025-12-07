package com.triappdriver.utils

import cocoapods.FirebaseAuth.*
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual fun getCurrentUserFirebase(): AppUser? =
    FIRAuth.auth().currentUser()?.toAppUser()

@OptIn(ExperimentalForeignApi::class)
actual suspend fun sendPasswordResetFirebase(email: String): Result<Unit> =
    try {
        suspendCancellableCoroutine { cont ->
            FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
                if (error != null) {
                    cont.resumeWithException(Exception(error.localizedDescription))
                } else {
                    cont.resume(Unit)
                }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

@OptIn(ExperimentalForeignApi::class)
actual suspend fun verifyResetCodeFirebase(code: String): Result<Unit> =
    try {
        suspendCancellableCoroutine { cont ->
            FIRAuth.auth().verifyPasswordResetCode(code) { result, error ->
                if (error != null) {
                    cont.resumeWithException(Exception(error.localizedDescription))
                } else {
                    cont.resume(Unit)
                }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun signOutFirebase(): Result<Unit> =
    try {
        memScoped {
            val errorPtr: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
            FIRAuth.auth().signOut(errorPtr)
            val error = errorPtr.pointed.value
            if (error != null) {
                throw Exception(error.localizedDescription)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


@OptIn(ExperimentalForeignApi::class)
actual suspend fun requestPhoneCodeFirebase(
    phone: String,
    activity: Any?
): Result<String> =
    try {
        val verificationId = suspendCancellableCoroutine<String> { cont ->
            FIRPhoneAuthProvider.provider().verifyPhoneNumber(
                phoneNumber = "+55$phone",
                UIDelegate = null,
                completion = { verificationID, error ->
                    when {
                        error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                        verificationID != null -> cont.resume(verificationID)
                        else -> cont.resumeWithException(Exception("Unknown error"))
                    }
                }
            )
        }
        Result.success(verificationId)
    } catch (e: Exception) {
        Result.failure(e)
    }


@OptIn(ExperimentalForeignApi::class)
actual suspend fun verifyPhoneCodeFirebase(verificationId: String, code: String): Result<Unit> =
    try {
        suspendCancellableCoroutine { cont ->
            val credential =
                FIRPhoneAuthProvider.provider().credentialWithVerificationID(verificationId, code)
            FIRAuth.auth().signInWithCredential(credential) { authResult, error ->
                if (error != null) {
                    cont.resumeWithException(Exception(error.localizedDescription))
                } else {
                    cont.resume(Unit)
                }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

@OptIn(ExperimentalForeignApi::class)
private fun FIRUser.toAppUser(): AppUser =
    AppUser(
        userId = uid(),
        displayName = displayName() ?: "Unknown",
        email = email() ?: "Unknown"
    )
