package com.triappdriver.utils

import com.triappdriver.domain.model.Coordinate

interface PlatformMap {
    fun moveCamera(coordinate: Coordinate, zoom: Double)
    fun addMarker(id: String, coordinate: Coordinate)
    fun moveMarker(id: String, coordinate: Coordinate)
    fun drawPolyline(id: String, polyline: List<Coordinate>)
}