package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.MapboxMaps.*
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
            val resourceOptions = ResourceOptions(accessToken = "SUA_CHAVE_MAPBOX")
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
            CameraOptions(center = Point(coordinate.lng, coordinate.lat), zoom = zoom)
        )
    }

    override fun addMarker(id: String, coordinate: Coordinate) {
        // Mapbox iOS ainda não tem plugin direto para annotations no ComposeView.
        // Aqui você poderia adicionar uma anotação manualmente via runtime.
    }

    override fun moveMarker(id: String, coordinate: Coordinate) {
        // Similar: atualizar posição do marcador.
    }

    override fun drawPolyline(id: String, polyline: List<Coordinate>) {
        // Implementação futura via runtime.
    }
}
