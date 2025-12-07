package com.triappdriver.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.*
import kotlin.coroutines.resume

class IOSNotificationManagerImpl : NotificationManager {

    //private val iosManager = IosNotificationManager()

    override suspend fun requestPermission(): Boolean = true
        // suspendCancellableCoroutine { continuation ->
          // iosManager.requestPermission { granted ->
          //     continuation.resume(granted)
          // }
        //}

    override fun showNotification(title: String, message: String) {
        // iosManager.showNotificationWithTitle(title, message)
    }
}
