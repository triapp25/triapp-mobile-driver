package com.triapp.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.triapp.local.dao.NotificationDao
import com.triapp.local.dao.RideDao
import com.triapp.local.entity.NotificationEntity
import com.triapp.local.entity.RideEntity

private const val DB_NAME = "multi_tenant_app.db"
private const val DB_VERSION = 1

@Database(
    entities = [
        NotificationEntity::class, // NOVO
        RideEntity::class          // NOVO
    ],
    version = DB_VERSION
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    // Novos DAOs
    abstract fun notificationDao(): NotificationDao
    abstract fun rideDao(): RideDao
}