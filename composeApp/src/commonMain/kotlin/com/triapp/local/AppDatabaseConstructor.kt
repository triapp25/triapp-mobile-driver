package com.triapp.local

import androidx.room.RoomDatabaseConstructor

expect class AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}