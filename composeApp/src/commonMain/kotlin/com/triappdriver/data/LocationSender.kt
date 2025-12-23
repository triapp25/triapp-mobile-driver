package com.triappdriver.data

import com.triappdriver.data.dtos.LocationRequest
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.utils.FirebaseAuthManager
import com.triappdriver.utils.GeoLocationTracker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample

class LocationSender(
    private val api: ApiService,
    private val geoLocationTracker: GeoLocationTracker,
    private val firebaseAuthManager: FirebaseAuthManager
) {

    @OptIn(FlowPreview::class)
    suspend fun start() {
        geoLocationTracker.coordinate
            .filterNotNull()
            .onStart {
                geoLocationTracker.coordinate.value?.let { emit(it) }
            }
            .sample(30_000)
            .collect { coordinate ->
                send(coordinate)
            }
    }

    private suspend fun send(coordinate: Coordinate) {
        api.sendLocation(
            LocationRequest(
                driverId = firebaseAuthManager.getCurrentUser()?.userId.orEmpty(),
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                status = "AVAILABLE"
            )
        )
    }

}