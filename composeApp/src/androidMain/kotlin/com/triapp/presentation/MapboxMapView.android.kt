package com.triapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

@Composable
actual fun MapboxMapView(modifier: Modifier) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS)
            mapView
        },
        modifier = modifier
    )
}