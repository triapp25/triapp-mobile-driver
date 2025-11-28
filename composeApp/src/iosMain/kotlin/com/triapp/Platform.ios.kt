package com.triapp

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual typealias PlatformContext = UIViewController

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}