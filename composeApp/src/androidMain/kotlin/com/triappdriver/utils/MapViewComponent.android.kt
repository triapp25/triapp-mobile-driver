package com.triappdriver.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils // Necessário para a decodificação da polyline
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.triappdriver.domain.model.Coordinate
import kotlin.math.*

// ----------------------------------------------------
// FUNÇÕES AUXILIARES KMP (Distância Haversine)
// Estas funções são necessárias para calcular o ETA/Zoom
// ----------------------------------------------------

private fun degreesToRadians(degrees: Double): Double {
    return degrees * PI / 180.0
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371 // Raio da Terra em km
    val latDistance = degreesToRadians(lat2 - lat1)
    val lonDistance = degreesToRadians(lon2 - lon1)
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(degreesToRadians(lat1)) * cos(degreesToRadians(lat2)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c * 1000 // Distância em metros
}

// ----------------------------------------------------
// GERENCIADORES (para acesso no bloco 'update')
// ----------------------------------------------------

private data class MapMarkersManager(
    val userManager: CircleAnnotationManager,
    val driverManager: CircleAnnotationManager,
    val polylineManager: PolylineAnnotationManager
)

@Composable
actual fun MapViewComponent(
    modifier: Modifier,
    coordinate: Coordinate, // Posição do motorista
    routePolyline: List<Coordinate>?, // Rota completa (motorista -> coleta -> destino)
    riderPosition: Coordinate?, // Posição atual do usuário
) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)

            // Configuração inicial e criação dos gerenciadores (rodando apenas uma vez)
            mapView.mapboxMap.loadStyle(Style.STANDARD) { _ ->
                val annotationPlugin = mapView.annotations

                val userManager = annotationPlugin.createCircleAnnotationManager()
                val driverManager = annotationPlugin.createCircleAnnotationManager()
                val polylineManager = annotationPlugin.createPolylineAnnotationManager()

                mapView.tag = MapMarkersManager(userManager, driverManager, polylineManager)

                // Câmera inicial no usuário
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
            val userPoint = Point.fromLngLat(coordinate.longitude, coordinate.latitude)
            val markers = mapView.tag as? MapMarkersManager ?: return@AndroidView

            // Variável para coletar os pontos que a câmera precisa enquadrar
            val pointsToIncludeInCamera = mutableListOf(userPoint)
            val FOCUS_THRESHOLD_METERS = 2000.0

            // ----------------------------------------------------
            // 1. ATUALIZAÇÃO DA POLYLINE
            // ----------------------------------------------------
            markers.polylineManager.deleteAll()
            routePolyline?.let { poly ->
                if (poly.isNotEmpty()) {
                    val polylinePoints = poly.map { Point.fromLngLat(it.latitude, it.longitude) }

                    val polylineOptions = PolylineAnnotationOptions()
                        .withPoints(polylinePoints)
                        .withLineWidth(6.0)
                        .withLineColor("#007AFF")

                    markers.polylineManager.create(polylineOptions)
                }
            }

            // ----------------------------------------------------
            // 2. CÁLCULO DINÂMICO DA CÂMERA
            // ----------------------------------------------------

            var distanceToDriver = Double.MAX_VALUE
            if (riderPosition != null) {
                val driverPoint = Point.fromLngLat(riderPosition.longitude, riderPosition.latitude)
                pointsToIncludeInCamera.add(driverPoint)

                distanceToDriver = calculateDistance(
                    coordinate.latitude, coordinate.longitude,
                    riderPosition.latitude, riderPosition.longitude
                )
            }

            val isDriverActive = riderPosition != null
            val isDriverApproaching = isDriverActive && distanceToDriver <= FOCUS_THRESHOLD_METERS
            val isRouteAvailable = routePolyline?.isNotEmpty() == true

            val cameraOptions = when {
                isDriverActive && isDriverApproaching -> {
                    // MODO 2 (Prioridade MÁXIMA): FOCO NO PICKUP (Motorista Perto do Usuário)
                    // Força o zoom-in no Usuário e no Driver.

                    val focusPoints = listOfNotNull(userPoint, riderPosition.let { c -> Point.fromLngLat(c.longitude, c.latitude) })

                    mapView.mapboxMap.cameraForCoordinates(
                        focusPoints,
                        EdgeInsets(200.0, 200.0, 200.0, 200.0), // Padding MAIOR para zoom
                        null,
                        null
                    )
                }
                isRouteAvailable -> {
                    // MODO 1: VISÃO GERAL (Rota Ativa, Zoom Out para mostrar o trajeto completo)

                    val allPointsForBounds = pointsToIncludeInCamera.filterNotNull().toMutableList()

                    // Adiciona o destino final
                    routePolyline!!.lastOrNull()?.let { lastPoint ->
                        allPointsForBounds.add(Point.fromLngLat(lastPoint.latitude, lastPoint.longitude))
                    }

                    // Enquadra a rota completa
                    mapView.mapboxMap.cameraForCoordinates(
                        allPointsForBounds.distinct(),
                        EdgeInsets(100.0, 100.0, 100.0, 100.0), // Padding normal
                        null,
                        null
                    )
                }
                isDriverActive -> {
                    // MODO 3: ACOMPANHAMENTO DO MOTORISTA (Se a rota sumir, mas o motorista ainda estiver lá)

                    CameraOptions.Builder()
                        .center(Point.fromLngLat(riderPosition!!.longitude, riderPosition.latitude))
                        .zoom(15.0)
                        .build()

                }
                else -> {
                    // MODO PADRÃO (Início ou Fim)
                    CameraOptions.Builder()
                        .center(userPoint)
                        .zoom(15.0)
                        .build()
                }
            }

            // 3. MOVER A CÂMERA SUAVEMENTE
            mapView.getMapboxMap().easeTo(
                cameraOptions,
                MapAnimationOptions.mapAnimationOptions { duration(1000) }
            )

            // 4. ATUALIZAÇÃO DOS MARCADORES (Usuário e Motorista)

            // Marcardor do Usuário
            markers.userManager.deleteAll()
            val userCircle = CircleAnnotationOptions()
                .withPoint(userPoint)
                .withCircleRadius(8.0)
                .withCircleColor("#34C759")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#FFFFFF")
            markers.userManager.create(userCircle)

            // Marcador do Motorista
            markers.driverManager.deleteAll()
            if (riderPosition != null) {
                val driverPoint = Point.fromLngLat(riderPosition.longitude, riderPosition.latitude)

                val driverCircle = CircleAnnotationOptions()
                    .withPoint(driverPoint)
                    .withCircleRadius(10.0)
                    .withCircleColor("#000000")
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#FFFFFF")

                markers.driverManager.create(driverCircle)
            }
        }
    )
}