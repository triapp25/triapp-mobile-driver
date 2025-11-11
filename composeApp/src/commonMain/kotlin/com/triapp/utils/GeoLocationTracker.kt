package com.triapp.utils

import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.triapp.domain.model.Coordinate

class GeoLocationTracker(
    private val locationTracker: LocationTracker
) {
    private val _coordinate = MutableStateFlow(Coordinate(-6.194186, 106.701598))
    val coordinate = _coordinate.asStateFlow()

    suspend fun startTracking() {
        locationTracker.startTracking()
        locationTracker.getLocationsFlow().collect { state ->
            val lat = state.latitude
            val lon = state.longitude
            _coordinate.value = Coordinate(lat, lon)
        }
    }

    fun stopTracking() {
        locationTracker.stopTracking()
    }
}

expect fun getLocationTracker(permissionController: PermissionsController): LocationTracker