package com.triapp.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

actual fun getCrashlyticsService(): CrashlyticsService = AndroidCrashlyticsService()

class AndroidCrashlyticsService : CrashlyticsService {
    override fun recordException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
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