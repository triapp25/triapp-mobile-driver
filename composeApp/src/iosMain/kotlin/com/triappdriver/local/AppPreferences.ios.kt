package com.triappdriver.local

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults
import com.russhwolf.settings.ObservableSettings

actual fun provideObservableSettings(context: Any?): ObservableSettings =
    NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
