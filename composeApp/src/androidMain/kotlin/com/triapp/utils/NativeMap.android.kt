package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.geojson.Point
import com.triapp.presentation.feature.home.MapController

@Composable
actual fun NativeMap(
    modifier: Modifier,
    onMapReady: (PlatformMap) -> Unit,
    mapController: MapController
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val mapView = MapView(context)
            val mapboxMap = mapView.getMapboxMap()
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                val controller = MapboxPlatformMap(mapView)
                onMapReady(controller)
            }
            mapView
        }
    )
}

class MapboxPlatformMap(mapView: MapView) : PlatformMap {
    private val mapboxMap = mapView.getMapboxMap()
    private val annotationManager = mapView.annotations.createPointAnnotationManager()

    override fun moveCamera(coordinate: Coordinate, zoom: Double) {
        mapboxMap.setCamera(
            com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(coordinate.lng, coordinate.lat))
                .zoom(zoom)
                .build()
        )
    }

    override fun addMarker(id: String, coordinate: Coordinate) {
        val point = Point.fromLngLat(coordinate.lng, coordinate.lat)
        val options = PointAnnotationOptions().withPoint(point)
        annotationManager.create(options)
    }

    override fun moveMarker(id: String, coordinate: Coordinate) {
        // Para simplificar: remove e recria o marcador
        annotationManager.deleteAll()
        addMarker(id, coordinate)
    }

    override fun drawPolyline(id: String, polyline: List<Coordinate>) {
        // Para um app real, use o plugin de linhas do Mapbox
    }
}
