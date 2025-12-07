package com.triappdriver

import androidx.compose.ui.window.ComposeUIViewController
import com.triappdriver.utils.mapboxViewController
import com.triappdriverdriver.App
import platform.UIKit.UIViewController

fun MainViewController(mapViewController: () -> UIViewController) = ComposeUIViewController(
    configure = {
        mapboxViewController = mapViewController
    }
) {
    App(activity = mapboxViewController?.invoke())

}