package com.triapp.utils

import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.NSObject
import kotlin.coroutines.resume

actual class LocationProvider : NSObject(), CLLocationManagerDelegateProtocol {

    private val locationManager = CLLocationManager()
    private var completion: ((Pair<Double, Double>?) -> Unit)? = null

    init {
        locationManager.delegate = this
    }

    actual suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { cont ->
            completion = { cont.resume(it) }
            locationManager.requestWhenInUseAuthorization()
            locationManager.requestLocation()
        }
    }

    @ObjCAction
    fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        val coords = location?.coordinate
        completion?.invoke(
            if (coords != null) Pair(coords.latitude, coords.longitude) else null
        )
        completion = null
    }

    @ObjCAction
    fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        completion?.invoke(null)
        completion = null
    }
}
