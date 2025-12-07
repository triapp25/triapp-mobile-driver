package com.triappdriver.utils


class LocationRepository(
    private val locationProvider: LocationProvider
) {
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return locationProvider.getCurrentLocation()
    }
    suspend fun requestLocationPermission(): Boolean {
        return locationProvider.requestLocationPermission()
    }
}


expect class LocationProvider() {
    suspend fun getCurrentLocation(): Pair<Double, Double>?
    suspend fun requestLocationPermission(): Boolean
}
