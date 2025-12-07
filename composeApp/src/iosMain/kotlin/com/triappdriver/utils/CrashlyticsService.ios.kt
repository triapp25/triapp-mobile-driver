package com.triappdriver.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

actual fun getCrashlyticsService(): CrashlyticsService = IOSCrashlyticsService()

class IOSCrashlyticsService : CrashlyticsService {
    override fun recordException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }

    override fun setUserId(userId: String) {
        Firebase.crashlytics.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun log(message: String) {
        Firebase.crashlytics.log(message)
    }
}