package com.triappdriver.utils

interface NotificationManager {
    suspend fun requestPermission(): Boolean
    fun showNotification(title: String, message: String)
}

expect fun getPlatformNotificationManager(context: Any? = null): NotificationManager