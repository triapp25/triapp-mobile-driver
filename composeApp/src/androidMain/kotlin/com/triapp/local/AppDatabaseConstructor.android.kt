package com.triapp.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabaseConstructor

actual class AppDatabaseConstructor(
    private val context: Context
) : RoomDatabaseConstructor<AppDatabase> {

    actual override fun initialize(): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "multi_tenant_app.db"
        ).build()
    }
}