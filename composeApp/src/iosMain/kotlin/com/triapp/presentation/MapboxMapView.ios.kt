package com.triapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
actual fun MapboxMapView(modifier: Modifier) {
    UIKitView(
        factory = {
            val resourceOptions = ResourceOptions(accessToken = "SUA_CHAVE_MAPBOX")
            val initOptions = MapInitOptions(resourceOptions = resourceOptions)
            val mapView = MapView(frame = CGRectZero.readValue(), mapInitOptions = initOptions)
            mapView.getMapboxMap().loadStyleURI(StyleURI.Streets)
            mapView
        },
        modifier = modifier
    )
}
