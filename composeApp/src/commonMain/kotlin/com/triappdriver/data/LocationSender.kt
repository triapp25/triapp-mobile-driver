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
        combine(
            geoLocationTracker.coordinate.filterNotNull(),
            appPreferences.isDriverActive(),
            appPreferences.isDriverCurrentTrip()
        ) { coord, active, currentTrip ->
            LocationState(
                coordinate = coord,
                isActive = active,
                currentTrip = currentTrip
            )
        }
            .sample(30_000)
            .collect { state ->
                val status = when {
                    !state.isActive -> "OFFLINE"
                    state.currentTrip.isNullOrEmpty() -> "AVAILABLE"
                    else -> "BUSY   "
                }

                api.sendLocation(
                    LocationRequest(
                        driverId = firebaseAuthManager
                            .getCurrentUser()
                            ?.userId
                            .orEmpty(),
                        latitude = state.coordinate.latitude,
                        longitude = state.coordinate.longitude,
                        status = status
                    )
                )
            }
    }

}

data class LocationState(
    val coordinate: Coordinate,
    val isActive: Boolean,
    val currentTrip: String?
)