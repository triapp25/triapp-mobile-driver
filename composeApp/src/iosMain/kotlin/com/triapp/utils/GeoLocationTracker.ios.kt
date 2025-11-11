package com.triapp.utils

import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController

actual fun getLocationTracker(permissionController: PermissionsController): LocationTracker {
    return LocationTracker(permissionController)
}