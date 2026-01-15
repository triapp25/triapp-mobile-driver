package com.triappdriver.presentation.feature.home

import androidx.lifecycle.viewModelScope
import com.triappdriver.data.dtos.LocationDTO
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.repository.MapboxSearchRepository
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.domain.model.HomeDomainModel
import com.triappdriver.domain.model.HomeStep
import com.triappdriver.domain.model.HomeStep.InProgress
import com.triappdriver.domain.model.HomeStep.NavigatingToPickup
import com.triappdriver.domain.model.HomeStep.Offline
import com.triappdriver.domain.model.HomeStep.OnlineSearching
import com.triappdriver.domain.model.HomeStep.RideCompleted
import com.triappdriver.domain.model.HomeStep.RideOffer
import com.triappdriver.domain.model.TaxiState
import com.triappdriver.domain.usecase.GetRatingLastUseCase
import com.triappdriver.domain.usecase.TaxiUseCase
import com.triappdriver.local.AppPreferences
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.GeoLocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(
    private val geoLocationTracker: GeoLocationTracker,
    private val mapboxRepository: MapboxSearchRepository,
    private val appPreferences: AppPreferences,
    private val taxiUseCase: TaxiUseCase,
    initialState: HomeDomainModel = HomeDomainModel()
) : BaseViewModel<HomeDomainModel, HomeIntent, HomeEffect>(initialState) {

    private var firestoreListenerJob: Job? = null
    private var currentTripId: String? = null

    init {
        observeLocation()
        restoreSession()
    }

    // --------------------------------------------------
    // LOCATION
    // --------------------------------------------------

    private fun observeLocation() {
        viewModelScope.launch {
            geoLocationTracker.coordinate.collect { loc ->
                loc ?: return@collect
                updateState {
                    it.copy(
                        driverPosition = Coordinate(loc.latitude, loc.longitude)
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // INTENTS
    // --------------------------------------------------

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.GoOnline -> goOnline()
            HomeIntent.GoOffline -> goOffline()
            HomeIntent.AcceptRide -> updateRideStatus("ACCEPTED")
            HomeIntent.RejectRide -> updateRideStatus("REJECTED")
            HomeIntent.StartRide -> updateRideStatus("ONGOING")
            HomeIntent.EndRide -> updateRideStatus("COMPLETED")
            else -> {}
        }
    }

    // --------------------------------------------------
    // ONLINE / OFFLINE
    // --------------------------------------------------

    private fun goOnline() {
        appPreferences.driverActive(true)
        updateState { it.copy(isOnline = true, step = OnlineSearching) }

        viewModelScope.launch {
            taxiUseCase.enabledOnline().collect {
                handleTaxiState(it)
            }
        }
    }

    private fun goOffline() {
        stopFirestoreListener()
        appPreferences.driverActive(false)
        appPreferences.driverCurrentTrip("")
        updateState { it.copy(isOnline = false, step = Offline) }
    }

    // --------------------------------------------------
    // FIRESTORE
    // --------------------------------------------------

    private fun startFirestoreListener() {
        if (firestoreListenerJob?.isActive == true) return

        firestoreListenerJob = viewModelScope.launch {
            taxiUseCase.startTaxiFlow(currentTripId.orEmpty())
                .collect { handleTaxiState(it) }
        }
    }

    private fun stopFirestoreListener() {
        firestoreListenerJob?.cancel()
        firestoreListenerJob = null
    }

    // --------------------------------------------------
    // TAXI STATE HANDLER
    // --------------------------------------------------

    private fun handleTaxiState(state: TaxiState) {
        when (state) {

            is TaxiState.Online -> showRideOffer(state.rider)

            is TaxiState.Update -> restoreFromUpdate(state)

            is TaxiState.Error -> {
                println("Erro Firestore: ${state.message}")
            }

            TaxiState.Idle -> {}
        }
    }

    // --------------------------------------------------
    // RESTORE FROM UPDATE (🔥 CHAVE DO SISTEMA 🔥)
    // --------------------------------------------------

    private fun restoreFromUpdate(update: TaxiState.Update) {
        val snapshot = update.snapshot

        currentTripId = snapshot.tripId
        appPreferences.driverCurrentTrip(snapshot.tripId)

        val pickup = Coordinate(snapshot.pickup.lat, snapshot.pickup.lng)
        val destination = Coordinate(snapshot.dropoff.lat, snapshot.dropoff.lng)

        updateState {
            it.copy(
                isOnline = true,
                currentRiderName = snapshot.rider.name,
                currentPassengerNote = snapshot.rider.note,
                currentFare = snapshot.fare,
                currentPickupAddress = snapshot.pickup.name,
                currentDestinationAddress = snapshot.dropoff.name,
                pickupCoordinate = pickup,
                destinationCoordinate = destination
            )
        }

        moveToStep(snapshot.status)
        updateRoute(
            status = snapshot.status,
            pickup = pickup,
            destination = destination
        )
    }

    // --------------------------------------------------
    // STEP MACHINE
    // --------------------------------------------------

    private fun moveToStep(status: String) {
        val step = when (status) {

            "REQUESTED" -> RideOffer(
                passengerName = state.value.currentRiderName.orEmpty(),
                pickupAddress = state.value.currentPickupAddress.orEmpty(),
                destinationAddress = state.value.currentDestinationAddress.orEmpty(),
                distanceToPickup = "",
                estimatedFare = state.value.currentFare.orEmpty(),
                eta = "",
                passengerNote = state.value.currentPassengerNote
            )

            "ACCEPTED", "ARRIVED" ->
                NavigatingToPickup(
                    passengerName = state.value.currentRiderName.orEmpty(),
                    pickupAddress = state.value.currentPickupAddress.orEmpty()
                )

            "ONGOING" ->
                InProgress(
                    passengerName = state.value.currentRiderName.orEmpty(),
                    destinationAddress = state.value.currentDestinationAddress.orEmpty(),
                    timeRemainingMinutes = state.value.etaMinutes
                )

            "COMPLETED" -> {
                clearTripKeepOnline()
                OnlineSearching
            }

            else -> OnlineSearching
        }

        updateState { it.copy(step = step) }
    }

    // --------------------------------------------------
    // ROUTE
    // --------------------------------------------------

    private fun updateRoute(
        status: String,
        pickup: Coordinate,
        destination: Coordinate
    ) {
        viewModelScope.launch {
            val driverPos = state.value.driverPosition ?: return@launch

            val target = when (status) {
                "ACCEPTED", "ARRIVED" -> pickup
                "ONGOING" -> destination
                else -> return@launch
            }

            val polyline = mapboxRepository
                .getRoutePolyline(driverPos, target)
                ?.second
                .orEmpty()

            updateState {
                it.copy(
                    routePolyline = polyline,
                    targetCoordinate = target
                )
            }
        }
    }

    // --------------------------------------------------
    // CLEANUP
    // --------------------------------------------------

    private fun clearTripKeepOnline() {
        stopFirestoreListener()
        currentTripId = null
        appPreferences.driverCurrentTrip("")

        updateState {
            it.copy(
                step = OnlineSearching,
                currentRiderName = null,
                currentRating = null,
                currentPickupAddress = null,
                currentDestinationAddress = null,
                currentPassengerNote = null,
                currentFare = null,
                pickupCoordinate = null,
                destinationCoordinate = null,
                routePolyline = emptyList(),
                targetCoordinate = null
            )
        }
    }

    // --------------------------------------------------
    // RESTORE SESSION (APP ABERTO)
    // --------------------------------------------------

    private fun restoreSession() {
        viewModelScope.launch {
            appPreferences.isDriverCurrentTrip().collect { tripId ->
                if (tripId.isBlank()) return@collect

                currentTripId = tripId
                startFirestoreListener()
            }
        }
    }

    // --------------------------------------------------
    // RIDE STATUS
    // --------------------------------------------------

    private fun updateRideStatus(status: String) {
        val tripId = currentTripId ?: return

        viewModelScope.launch {
            taxiUseCase.updateTripStatus(
                tripId,
                status,
                LocationDTO(
                    lat = state.value.driverPosition?.latitude ?: 0.0,
                    lng = state.value.driverPosition?.longitude ?: 0.0,
                    address = ""
                ),
                state.value.etaMinutes,
                state.value.distanceMeters
            )
        }
    }

    private fun showRideOffer(rider: RiderFirebaseDTO) {
        currentTripId = rider.tripId
        startFirestoreListener()

        updateState {
            it.copy(
                step = RideOffer(
                    passengerName = rider.name.orEmpty(),
                    pickupAddress = rider.pickup.name.orEmpty(),
                    destinationAddress = rider.rider.location.name.orEmpty(),
                    distanceToPickup = rider.distance.orEmpty(),
                    estimatedFare = rider.fare.orEmpty(),
                    eta = rider.etaMin.orEmpty(),
                    passengerNote = rider.rider.note
                )
            )
        }
    }
}