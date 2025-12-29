package com.triappdriver.utils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocationProvider {

    actual suspend fun requestLocationPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val activity = currentActivityRef?.get()
            ?: run {
                // Se não houver Activity registrada, assumimos que falhou.
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

        // 1. Verificar se a permissão já foi concedida
        val permission = Manifest.permission.ACCESS_FINE_LOCATION // Usamos FINE como primário
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }

        // 2. Se não concedida, registrar o launcher para solicitá-la
        val launcher = activity.activityResultRegistry.register(
            "LocationPermissionRequest_${System.currentTimeMillis()}",
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            // 3. Resumir a coroutine com o resultado
            cont.resume(isGranted)
        }

        // Cancelar o launcher se a coroutine for cancelada
        cont.invokeOnCancellation {
            launcher.unregister()
        }

        // 4. Lançar o pedido de permissão
        launcher.launch(permission)
    }

    @androidx.annotation.RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    actual suspend fun getCurrentLocation(): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        val activity = currentActivityRef?.get() ?: run {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val client = LocationServices.getFusedLocationProviderClient(activity)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000
        )
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation

                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                    client.removeLocationUpdates(this)
                }
            }
        }

        client.requestLocationUpdates(
            request,
            callback,
            activity.mainLooper
        )

        cont.invokeOnCancellation {
            client.removeLocationUpdates(callback)
        }
    }
}