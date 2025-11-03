package com.triapp.utils

actual fun getPlatformNotificationManager(context: Any?): NotificationManager =
    IOSNotificationManager()