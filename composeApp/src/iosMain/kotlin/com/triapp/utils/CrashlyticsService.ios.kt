package com.triapp.utils

import platform.FirebaseCrashlytics.FIRCrashlytics

actual fun getCrashlyticsService(): CrashlyticsService = IOSCrashlyticsService()

class IOSCrashlyticsService : CrashlyticsService {
    override fun recordException(throwable: Throwable) {
        // Exemplo usando a API nativa de iOS
        FIRCrashlytics.crashlytics().recordExceptionModel(
            exceptionModel = throwable.asFirebaseExceptionModel() // Exige uma conversão, ou use GitLive
        )
    }

    override fun setUserId(userId: String) {
        TODO("Not yet implemented")
    }

    override fun setCustomKey(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun log(message: String) {
        TODO("Not yet implemented")
    }
}