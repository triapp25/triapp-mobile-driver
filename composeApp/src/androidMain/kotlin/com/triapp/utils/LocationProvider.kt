package com.triapp.utils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocationProvider(private val context: Context) {

    actual suspend fun requestLocationPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val activity = currentActivityRef?.get()
            ?: run {
                // Se não houver Activity registrada, assumimos que falhou.
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

        // 1. Verificar se a permissão já foi concedida
        val permission = Manifest.permission.ACCESS_FINE_LOCATION // Usamos FINE como primário
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
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

    actual suspend fun getCurrentLocation(): Pair<Double, Double>? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        // Verificação de permissão agora só garante que podemos prosseguir
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            // Retorna null ou lança uma exceção, o ViewModel deve chamar requestLocationPermission antes
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }.addOnFailureListener {
                cont.resume(null)
            }
        }
    }
}