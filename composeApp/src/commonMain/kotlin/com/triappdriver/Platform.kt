package com.triappdriver

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class PlatformContext

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
