package com.triapp.utils

import android.content.Context

actual fun getPlatformNotificationManager(context: Any?): NotificationManager {
    require(context is Context) { "Android context is required" }
    return AndroidNotificationManager(context = context)
}
