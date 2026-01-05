package com.triappdriver.presentation.feature.home

import com.triappdriver.domain.model.Coordinate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Modelo simples de LatLng (use o seu se já existir)
data class LatLng(
    val lat: Double,
    val lng: Double
)

data class RouteMetrics(
    val totalDistanceMeters: Double,
    val traveledDistanceMeters: Double,
    val remainingDistanceMeters: Double,
    val progress: Float,
    val etaMinutes: Int
)

object RouteMetricsCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val AVERAGE_SPEED_KMH = 30.0

    /**
     * Calcula:
     * - distância total
     * - distância percorrida
     * - distância restante
     * - progresso (0f..1f)
     * - ETA em minutos
     */
    fun calculate(
        polyline: List<Coordinate>,
        currentPosition: Coordinate
    ): RouteMetrics {

        if (polyline.isEmpty()) {
            return RouteMetrics(
                totalDistanceMeters = 0.0,
                traveledDistanceMeters = 0.0,
                remainingDistanceMeters = 0.0,
                progress = 0f,
                etaMinutes = 0
            )
        }

        val totalDistance = totalRouteDistance(polyline)
        val traveledDistance = distanceTraveled(polyline, currentPosition)

        val remainingDistance =
            (totalDistance - traveledDistance).coerceAtLeast(0.0)

        val speedMs = AVERAGE_SPEED_KMH * 1000 / 3600
        val etaMinutes =
            ((remainingDistance / speedMs) / 60)
                .toInt()
                .coerceAtLeast(1)

        val progress =
            (traveledDistance / totalDistance)
                .toFloat()
                .coerceIn(0f, 1f)

        return RouteMetrics(
            totalDistanceMeters = totalDistance,
            traveledDistanceMeters = traveledDistance,
            remainingDistanceMeters = remainingDistance,
            progress = progress,
            etaMinutes = etaMinutes
        )
    }

    // ----------------------------------------------------
    // DISTÂNCIA TOTAL DA ROTA
    // ----------------------------------------------------
    private fun totalRouteDistance(polyline: List<Coordinate>): Double {
        var total = 0.0
        for (i in 0 until polyline.lastIndex) {
            total += haversineDistance(
                polyline[i],
                polyline[i + 1]
            )
        }
        return total
    }

    // ----------------------------------------------------
    // DISTÂNCIA JÁ PERCORRIDA (BASEADA NA POLYLINE)
    // ----------------------------------------------------
    private fun distanceTraveled(
        polyline: List<Coordinate>,
        currentPosition: Coordinate
    ): Double {

        var traveled = 0.0
        var closestIndex = 0
        var minDistance = Double.MAX_VALUE

        // Encontra o ponto da rota mais próximo do motorista
        polyline.forEachIndexed { index, point ->
            val distance = haversineDistance(currentPosition, point)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = index
            }
        }

        // Soma a distância até o ponto mais próximo
        for (i in 0 until closestIndex) {
            traveled += haversineDistance(
                polyline[i],
                polyline[i + 1]
            )
        }

        return traveled
    }

    // ----------------------------------------------------
    // HAVERSINE
    // ----------------------------------------------------
    private fun haversineDistance(a: Coordinate, b: Coordinate): Double {
        val dLat = degreesToRadians(b.latitude - a.latitude)
        val dLon = degreesToRadians(b.longitude - a.longitude)

        val lat1 = degreesToRadians(a.latitude)
        val lat2 = degreesToRadians(b.latitude)

        val sinLat = sin(dLat / 2)
        val sinLon = sin(dLon / 2)

        val h =
            sinLat * sinLat +
                    cos(lat1) * cos(lat2) *
                    sinLon * sinLon

        val c = 2 * atan2(sqrt(h), sqrt(1 - h))

        return EARTH_RADIUS_METERS * c
    }

    private fun degreesToRadians(degrees: Double): Double {
        return degrees * (PI / 180.0)
    }
}