package com.triapp

import android.app.Application
import com.triapp.di.androidModule
import com.triapp.di.initKoin
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