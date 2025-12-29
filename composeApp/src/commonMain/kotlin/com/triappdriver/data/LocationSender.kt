package com.triappdriver.data

import com.triappdriver.data.dtos.LocationRequest
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.local.AppPreferences
import com.triappdriver.utils.FirebaseAuthManager
import com.triappdriver.utils.GeoLocationTracker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample

class LocationSender(
    private val api: ApiIngestionService,
    private val geoLocationTracker: GeoLocationTracker,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val appPreferences: AppPreferences
) {

    @OptIn(FlowPreview::class)
    suspend fun start() {
        geoLocationTracker.coordinate
            .filterNotNull()
            .combine(appPreferences.isDriverActive()) { coord, active ->
                coord to active
            }
            .sample(30_000)
            .collect { (coordinate, active) ->
                println("📍 coordinate : $coordinate")
                println("📍 active : $active")
                api.sendLocation(
                    LocationRequest(
                        driverId = firebaseAuthManager.getCurrentUser()?.userId.orEmpty(),
                        latitude = coordinate.latitude,
                        longitude = coordinate.longitude,
                        status = if (active) "BUSY" else "AVAILABLE"
                    )
                )
            }
    }
}