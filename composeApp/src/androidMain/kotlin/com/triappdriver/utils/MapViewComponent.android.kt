package com.triappdriver.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
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
import com.triappdriver.domain.model.Coordinate

// Classe auxiliar para guardar os gerenciadores na Tag da View
private data class MapMarkersManager(
    val userManager: CircleAnnotationManager,
    val driverManager: CircleAnnotationManager
)

@Composable
actual fun MapViewComponent(
    modifier: Modifier,
    coordinate: Coordinate,
    driverCoordinate: Coordinate?
) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)

            // Configuração inicial
            mapView.mapboxMap.loadStyle(Style.STANDARD) { _ ->
                val annotationPlugin = mapView.annotations

                // Criamos DOIS gerenciadores separados
                val userManager = annotationPlugin.createCircleAnnotationManager()
                val driverManager = annotationPlugin.createCircleAnnotationManager()

                // Guardamos ambos na tag para recuperar no 'update'
                mapView.tag = MapMarkersManager(userManager, driverManager)

                // Centraliza câmera inicial no usuário
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(coordinate.longitude, coordinate.latitude))
                        .zoom(15.0)
                        .build()
                )
            }
            mapView
        },
        modifier = modifier,
        update = { mapView ->
            // 1. Movimentação da Câmera (Segue o usuário)
            // Nota: Para uma experiência melhor, você poderia calcular o ponto médio
            // entre usuário e motorista, mas vamos focar no usuário por enquanto.
            val userPoint = Point.fromLngLat(coordinate.longitude, coordinate.latitude)

            mapView.getMapboxMap().easeTo(
                CameraOptions.Builder()
                    .center(userPoint)
                    .zoom(15.0)
                    .build(),
                MapAnimationOptions.mapAnimationOptions { duration(1000) }
            )

            // 2. Atualização dos Marcadores
            val markers = mapView.tag as? MapMarkersManager

            markers?.let { managers ->
                // --- ATUALIZA MARCADOR DO USUÁRIO (Azul/Vermelho) ---
                managers.userManager.deleteAll()
                val userCircle = CircleAnnotationOptions()
                    .withPoint(userPoint)
                    .withCircleRadius(8.0)
                    .withCircleColor("#34C759") // Verde (Accent) ou a cor que preferir
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#FFFFFF")
                managers.userManager.create(userCircle)

                // --- ATUALIZA MARCADOR DO MOTORISTA (Carro) ---
                managers.driverManager.deleteAll()

                if (driverCoordinate != null) {
                    val driverPoint = Point.fromLngLat(driverCoordinate.longitude, driverCoordinate.latitude)

                    // Aqui você idealmente usaria .withIconImage() se tiver um Bitmap do carro
                    // Como fallback, usaremos um círculo Preto para representar o carro
                    val driverCircle = CircleAnnotationOptions()
                        .withPoint(driverPoint)
                        .withCircleRadius(10.0)
                        .withCircleColor("#000000") // Preto (Carro)
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#FFFFFF")

                    managers.driverManager.create(driverCircle)
                }
            }
        }
    )
}