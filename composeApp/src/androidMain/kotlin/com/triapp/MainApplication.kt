package com.triapp

import android.app.Activity
import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.triapp.di.initKoin
import com.triapp.utils.FirebaseAuthManagerImpl
import com.triapp.utils.FirebaseAuthManager
import com.triapp.utils.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            modules(module {
                single { LocationProvider(androidContext()) }

                factory<FirebaseAuthManager> {
                    FirebaseAuthManagerImpl(
                        FirebaseAuth.getInstance()
                    )
                }
            })
            androidContext(this@MainApplication)
            androidLogger()
        }
    }
}