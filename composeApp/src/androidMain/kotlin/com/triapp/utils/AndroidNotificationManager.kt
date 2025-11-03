package com.triapp.utils

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidNotificationManager(private val context: Context) : NotificationManager {

    private val channelId = "default_channel"

    override suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val granted = context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return@withContext false
            }
        }
        true
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun showNotification(title: String, message: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(0, notification)
    }
}
