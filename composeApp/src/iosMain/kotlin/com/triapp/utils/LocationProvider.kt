package com.triapp.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationProvider : NSObject(), CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private var permissionCompletion: ((Boolean) -> Unit)? = null
    private var locationCompletion: ((Pair<Double, Double>?) -> Unit)? = null

    init {
        locationManager.delegate = this
    }

    actual suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { cont ->
            locationCompletion = { cont.resume(it) }
            locationManager.requestLocation()
        }
    }

    actual suspend fun requestLocationPermission(): Boolean {
        return suspendCancellableCoroutine { cont ->
            val status = CLLocationManager.authorizationStatus()
            when (status) {
                kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> {
                    cont.resume(true)
                }
                kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                    cont.resume(false)
                }
                kCLAuthorizationStatusNotDetermined -> {
                    permissionCompletion = { granted -> cont.resume(granted) }
                    locationManager.requestWhenInUseAuthorization()
                }
                else -> cont.resume(false)
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        if (didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
            didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedAlways) {
            permissionCompletion?.invoke(true)
        } else if (didChangeAuthorizationStatus == kCLAuthorizationStatusDenied ||
            didChangeAuthorizationStatus == kCLAuthorizationStatusRestricted) {
            permissionCompletion?.invoke(false)
        }
        permissionCompletion = null
    }
}