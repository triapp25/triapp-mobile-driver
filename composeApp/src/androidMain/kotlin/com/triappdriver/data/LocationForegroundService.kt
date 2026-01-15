package com.triappdriver.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import com.triappdriver.R
import com.triappdriver.utils.GeoLocationTracker
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class LocationForegroundService : Service() {

    private val geoLocationTracker: GeoLocationTracker by inject()
    private val locationSender: LocationSender by inject()

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                123456,
                createLocationNotification(this),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(123456, createLocationNotification(this))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Primeiro garantimos o Foreground (já feito no onCreate, mas reforçamos aqui se necessário)

        serviceScope.launch {
            // Pequeno delay para garantir que o Android registrou o ForegroundServiceType.LOCATION
            delay(500)

            // 2. Inicia o tracker. Como é um 'single', a UI e o Service usarão o mesmo.
            geoLocationTracker.startTracking()
        }

        serviceScope.launch {
            // O LocationSender vai coletar do mesmo Flow que o tracker está alimentando
            locationSender.start()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        geoLocationTracker.stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopEverything()
        super.onTaskRemoved(rootIntent)
    }

    private fun stopEverything() {
        geoLocationTracker.stopTracking()
        serviceScope.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

}

fun createLocationNotification(context: Context): Notification {
    val channelId = "location_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Location tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    return NotificationCompat.Builder(context, channelId)
        .setContentTitle("Rastreamento ativo")
        .setContentText("Enviando localização")
        .setSmallIcon(R.drawable.splash_icon)
        .build()
}
