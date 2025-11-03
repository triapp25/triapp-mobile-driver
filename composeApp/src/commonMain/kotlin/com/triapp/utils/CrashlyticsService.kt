package com.triapp.utils

interface CrashlyticsService {
    fun recordException(throwable: Throwable)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
    fun log(message: String)
}

expect fun getCrashlyticsService(): CrashlyticsService