package com.triappdriver.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import com.triappdriver.domain.model.Coordinate
import platform.UIKit.UIViewController

@Composable
actual fun MapViewComponent(
    modifier: Modifier,
    coordinate: Coordinate
) {
    UIKitViewController<UIViewController>(
        factory = { mapboxViewController?.invoke() ?: UIViewController() },
        modifier = modifier.fillMaxSize(),
        update = { controller ->
            (controller as? MapboxViewController)?.onUpdateLocation(coordinate)
        }
    )
}