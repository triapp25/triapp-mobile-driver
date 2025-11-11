package com.triapp.presentation.feature.home

import com.triapp.domain.model.Coordinate
import com.triapp.domain.model.LatLng
import com.triapp.domain.model.RideOption
import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent
import com.triapp.utils.PlatformMap

sealed class HomeIntent : ViewIntent<Nothing> {
    object EnterDestination : HomeIntent()
    data class UpdatePickup(val pickup: String) : HomeIntent()
    data class UpdateDropoff(val dropoff: String) : HomeIntent()
    data class SelectOption(val option: RideOption) : HomeIntent()
    object ConfirmRequest : HomeIntent()
    object CancelRide : HomeIntent()
    object SimulateArrival : HomeIntent()
    object StartSimulation : HomeIntent()
    object StopSimulation : HomeIntent()
}


sealed class HomeEffect : SideEffect<Nothing> {
    data class ShowError(val message: String) : HomeEffect()
    object NavigateToConfirmation : HomeEffect()
}

enum class HomeStep { Idle, SelectingDestination, ChoosingRide, InProgress }

interface MapController {
    fun attachMap(map: PlatformMap)
    fun addMarker(id: String, position: LatLng)
    fun moveMarker(id: String, position: LatLng)
    fun drawPolyline(id: String, polyline: List<LatLng>)
    fun moveCamera(lat: Double, lng: Double, zoom: Double)
    fun moveDriver(lat: Double, lng: Double)
}

class DefaultMapController : MapController {
    private var platformMap: PlatformMap? = null

    override fun attachMap(map: PlatformMap) {
        platformMap = map
    }

    override fun addMarker(id: String, position: LatLng) {
        platformMap?.addMarker(id, Coordinate(position.lat, position.lng))
    }

    override fun moveMarker(id: String, position: LatLng) {
        platformMap?.moveMarker(id, Coordinate(position.lat, position.lng))
    }

    override fun drawPolyline(id: String, polyline: List<LatLng>) {
        platformMap?.drawPolyline(id, polyline.map { Coordinate(it.lat, it.lng) })
    }

    override fun moveCamera(lat: Double, lng: Double, zoom: Double) {
        platformMap?.moveCamera(Coordinate(lat, lng), zoom)
    }

    override fun moveDriver(lat: Double, lng: Double) {
        moveMarker("driver", LatLng(lat, lng))
    }
}
