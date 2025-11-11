package com.triapp

import android.app.Application
import com.triapp.di.initKoin
import com.triapp.local.appContext
import com.triapp.utils.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        initKoin {
            androidContext(this@MainApplication)
            androidLogger()
        }
    }
}