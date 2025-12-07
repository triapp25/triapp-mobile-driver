package com.triappdriver

import android.app.Application
import com.triappdriver.di.androidModule
import com.triappdriver.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(androidModule)
        }
    }
}