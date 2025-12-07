package com.triappdriver.utils

actual fun getPlatformNotificationManager(context: Any?): NotificationManager =
    IOSNotificationManagerImpl()