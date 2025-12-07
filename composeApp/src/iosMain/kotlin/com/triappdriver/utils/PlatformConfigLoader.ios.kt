package com.triappdriver.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual class PlatformConfigLoader {
    actual suspend fun loadConfigJson(): String = withContext(Dispatchers.Default) {
        val path = NSBundle.mainBundle.pathForResource("config", "json")
            ?: error("Arquivo config.json não encontrado no bundle iOS")

        val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            ?: error("Falha ao ler o conteúdo de config.json")

        return@withContext content as String
    }
}