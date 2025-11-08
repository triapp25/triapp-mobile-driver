package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.triapp.presentation.feature.home.MapController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeMap(
    modifier: Modifier,
    onMapReady: (PlatformMap) -> Unit,
    mapController: MapController
) {
    UIKitView(
        modifier = modifier,
        factory = {
            val resourceOptions = ResourceOptions(accessToken = "sk.eyJ1Ijoia3Nkcm9mNTAwIiwiYSI6ImNtaGd5ZHhtMjBrb24ycnB5Z3hmaDQxaGMifQ.rg9-3efU32R3dCv-ahAPJw")
            val initOptions = MapInitOptions(resourceOptions = resourceOptions)
            val mapView = MapView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), mapInitOptions = initOptions)
            mapView.mapboxMap.loadStyleURI(StyleURI.Streets) {
                val controller = IOSPlatformMap(mapView)
                onMapReady(controller)
            }
            mapView
        }
    )
}

class IOSPlatformMap(private val mapView: MapView) : PlatformMap {
    override fun moveCamera(coordinate: Coordinate, zoom: Double) {
        mapView.mapboxMap.setCamera(
            CameraOptions(
                center = Point(coordinate.lng, coordinate.lat),
                zoom = zoom
            )
        )
    }

    override fun addMarker(id: String, coordinate: Coordinate) {
        // Implementação futura: usar PointAnnotationManager se disponível via cinterop.
    }

    override fun moveMarker(id: String, coordinate: Coordinate) {
        // Implementação futura.
    }

    override fun drawPolyline(id: String, polyline: List<Coordinate>) {
        // Implementação futura.
    }
}