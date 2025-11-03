package com.triapp.local

import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

fun createAndroidObservableSettings(context: Context): ObservableSettings {
    val sharedPrefs = context.getSharedPreferences("triapp_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPrefs)
}


actual fun provideObservableSettings(context: Any?): ObservableSettings =
    createAndroidObservableSettings(context as Context)
