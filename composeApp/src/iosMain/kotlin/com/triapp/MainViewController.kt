package com.triapp

import androidx.compose.ui.window.ComposeUIViewController
import com.triapp.di.appModule
import com.triapp.utils.mapboxViewController
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(mapViewController: () -> UIViewController) = ComposeUIViewController(
    configure = {
        mapboxViewController = mapViewController
    }
) {
    App(activity = mapboxViewController?.invoke())

}