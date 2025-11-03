package com.triapp.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.*

class IOSNotificationManager : NotificationManager {

    override suspend fun requestPermission(): Boolean {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        return suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, _ ->
                continuation.resume(granted) {}
            }
        }
    }

    override fun showNotification(title: String, message: String) {
        val content = UNMutableNotificationContent().apply {
            this.title = title
            this.body = message
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "local_notification",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }
}
