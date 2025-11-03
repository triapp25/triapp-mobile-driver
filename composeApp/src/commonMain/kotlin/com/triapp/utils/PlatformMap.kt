package com.triapp.utils

data class Coordinate(val lat: Double, val lng: Double)

interface PlatformMap {
    fun moveCamera(coordinate: Coordinate, zoom: Double)
    fun addMarker(id: String, coordinate: Coordinate)
    fun moveMarker(id: String, coordinate: Coordinate)
    fun drawPolyline(id: String, polyline: List<Coordinate>)
}