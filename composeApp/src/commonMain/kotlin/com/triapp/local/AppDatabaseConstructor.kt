package com.triapp.local

import androidx.room.RoomDatabaseConstructor

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
