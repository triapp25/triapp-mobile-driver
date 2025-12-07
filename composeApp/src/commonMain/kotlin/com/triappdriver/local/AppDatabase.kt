package com.triappdriver.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.triappdriver.local.dao.NotificationDao
import com.triappdriver.local.dao.RatingDao
import com.triappdriver.local.dao.RideDao
import com.triappdriver.local.entity.NotificationEntity
import com.triappdriver.local.entity.RatingEntity
import com.triappdriver.local.entity.RideEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

private const val DB_NAME = "multi_tenant_app.db"
private const val DB_VERSION = 1

@Database(
    entities = [
        NotificationEntity::class,
        RideEntity::class,
        RatingEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class) // Aponta para o objeto expect abaixo
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun rideDao(): RideDao
    abstract fun ratingDao(): RatingDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver()) // Usa o driver do sqlite-bundled
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}