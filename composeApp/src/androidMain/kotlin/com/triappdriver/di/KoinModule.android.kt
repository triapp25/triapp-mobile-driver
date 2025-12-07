package com.triappdriver.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

import androidx.room.Room
import androidx.room.RoomDatabase
import com.russhwolf.settings.ObservableSettings
import com.triappdriver.local.AppDatabase
import com.triappdriver.local.provideObservableSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun getPlatformHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Android
}

val androidModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("multi_tenant_app.db")

        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }

    single<ObservableSettings> {
        provideObservableSettings(androidContext())
    }
}