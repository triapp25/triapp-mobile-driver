package com.triapp.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabaseConstructor

lateinit var appContext: Context

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "multi_tenant_app.db"
        ).build()
    }
}