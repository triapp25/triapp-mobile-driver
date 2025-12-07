package com.triappdriver.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.triappdriver.local.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUserDefaults

fun initKoin() {
    startKoin {
        modules(appModule + iosModule)
    }
}

val iosModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFilePath = documentDirectory() + "/multi_tenant_app.db"

        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
            factory = { AppDatabase::class.instantiateImpl() }
        )
    }

    single<ObservableSettings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documentDirectory?.path)
}