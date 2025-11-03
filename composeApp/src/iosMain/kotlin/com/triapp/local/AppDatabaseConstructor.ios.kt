package com.triapp.local

import androidx.room.Room
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

actual class AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {

    actual override fun initialize(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/multi_tenant_app.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}