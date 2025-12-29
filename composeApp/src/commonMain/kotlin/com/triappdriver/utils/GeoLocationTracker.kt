package com.triappdriver.utils

import com.triappdriver.domain.model.Coordinate
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeoLocationTracker(
    private val repository: LocationRepository
) {
    private val _coordinate = MutableStateFlow<Coordinate?>(null)
    val coordinate: StateFlow<Coordinate?> = _coordinate.asStateFlow()

    // Escopo que sobrevive enquanto o app estiver vivo
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isTracking = false

    fun startTracking() {
        if (isTracking) return
        isTracking = true

        scope.launch {
            if (!repository.requestLocationPermission()) return@launch

            while (isTracking) {
                repository.getCurrentLocation()?.let {
                    println("📍 NOVA localização: $it")
                    _coordinate.value = Coordinate(it.first, it.second)
                }
                delay(10_000)
            }
        }
    }

    fun stopTracking() {
        isTracking = false
    }
}


expect fun getLocationTracker(permissionController: PermissionsController): LocationTracker