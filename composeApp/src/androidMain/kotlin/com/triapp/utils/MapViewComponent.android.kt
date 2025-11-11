package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.triapp.domain.model.Coordinate
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager

@Composable
actual fun MapViewComponent(
    modifier: Modifier,
    coordinate: Coordinate
) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            val point = Point.fromLngLat(coordinate.longitude, coordinate.latitude)

            mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { _ ->

                val annotationPlugin = mapView.annotations
                val circleAnnotationManager = annotationPlugin.createCircleAnnotationManager()

                val circle = CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(10.0)
                    .withCircleColor("#FF0000")
                    .withCircleStrokeWidth(3.0)
                    .withCircleStrokeColor("#FFFFFF")

                circleAnnotationManager.create(circle)

                mapView.tag = circleAnnotationManager

                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(15.0)
                        .build()
                )
            }

            mapView
        },
        modifier = modifier,
        update = { mapView ->
            val point = Point.fromLngLat(coordinate.longitude, coordinate.latitude)
            val map = mapView.getMapboxMap()

            map.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build(),
                MapAnimationOptions.mapAnimationOptions { duration(1000) }
            )

            val manager = mapView.tag as? CircleAnnotationManager
            manager?.let {
                it.deleteAll()
                val circle = CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(10.0)
                    .withCircleColor("#FF0000")
                    .withCircleStrokeWidth(3.0)
                    .withCircleStrokeColor("#FFFFFF")
                it.create(circle)
            }
        }
    )
}