package com.triappdriver.presentation.feature.home

import com.triappdriver.presentation.SideEffect
import com.triappdriver.presentation.ViewIntent

sealed class HomeIntent : ViewIntent<Nothing> {
        object GoOnline : HomeIntent()
        object GoOffline : HomeIntent()


        object RejectRide : HomeIntent()
        object AcceptRide : HomeIntent()


        object ArrivedAtPickup : HomeIntent()
        object StartRide : HomeIntent()
        object EndRide : HomeIntent()
        object AcceptEarlyRide : HomeIntent()


        object StartSimulation : HomeIntent()
        object SimulateArrival : HomeIntent()
}


sealed class HomeEffect : SideEffect<Nothing> {
    data class ShowError(val msg: String) : HomeEffect()
    object NavigateToSummary : HomeEffect()
    object NavigateToConfirmation : HomeEffect()
}