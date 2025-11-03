package com.triapp.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

actual fun getPlatformHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Android
}