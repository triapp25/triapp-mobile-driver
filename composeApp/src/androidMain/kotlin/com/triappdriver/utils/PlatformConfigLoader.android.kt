package com.triappdriver.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

actual class PlatformConfigLoader(
    private val context: Context
) {
    actual suspend fun loadConfigJson(): String = withContext(Dispatchers.IO) {
        val inputStream = context.assets.open("config.json")
        return@withContext InputStreamReader(inputStream).readText()
    }
}