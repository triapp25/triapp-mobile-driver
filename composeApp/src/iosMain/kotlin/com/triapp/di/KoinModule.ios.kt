package com.triapp.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun getPlatformHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Darwin
}