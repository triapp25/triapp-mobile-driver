package com.triappdriver.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.triappdriver.local.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    val notificationManager: NotificationManager = getPlatformNotificationManager(this)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        val notification = NotificationEntity(
            title = title.orEmpty(),
            body = body.orEmpty(),
            deepLink = "",
            timestamp = System.currentTimeMillis()
        )

        // todo save on room

        scope.launch {

            sendNotification(
                title.orEmpty(), body.orEmpty()
            )
        }
    }

    override fun onNewToken(token: String) {

    }

    suspend fun sendNotification(title: String, body: String) {
        val granted = notificationManager.requestPermission()
        if (granted) {
            notificationManager.showNotification(title, body)
        }
    }
}
