package com.triapp.presentation.feature.home

import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.DriverInfo
import com.triapp.domain.model.HomeDomainModel
import com.triapp.domain.model.LatLng
import com.triapp.domain.model.RideInfo
import com.triapp.presentation.BaseViewModel
import com.triapp.utils.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationRepository: LocationRepository,
    initialState: HomeDomainModel = HomeDomainModel()
) : BaseViewModel<HomeDomainModel, HomeIntent, HomeEffect>(initialState) {

    private var simulationJob: Job? = null

    init {
        fetchUserLocation()
    }

    fun fetchUserLocation() {
        viewModelScope.launch {
            // PASSO 1: Solicitar/Verificar Permissão
            val permissionGranted = locationRepository.requestLocationPermission()

            if (permissionGranted) {
                // PASSO 2: Buscar Localização
                val location = locationRepository.getCurrentLocation()

                if (location != null) {
                    println("Latitude: ${location.first}, Longitude: ${location.second}")
                    updateState { it.copy(pickup = "LatLng(location.first, location.second)") }
                } else {
                    updateState { it.copy(error = "Não foi possível obter localização") }
                }
            } else {
                // Usuário negou a permissão
                updateState { it.copy(error = "Permissão de localização negada. Por favor, conceda nas configurações do aplicativo.") }
            }
        }
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.EnterDestination -> updateState { it.copy(step = HomeStep.SelectingDestination) }
            is HomeIntent.UpdatePickup -> updateState { it.copy(pickup = intent.pickup) }
            is HomeIntent.UpdateDropoff -> updateState { it.copy(dropoff = intent.dropoff) }
            is HomeIntent.SelectOption -> updateState {
                it.copy(
                    selectedOption = intent.option,
                    step = HomeStep.ChoosingRide
                )
            }

            HomeIntent.ConfirmRequest -> requestRide()
            HomeIntent.CancelRide -> cancelRide()
            HomeIntent.SimulateArrival -> simulateArrival()
            HomeIntent.StartSimulation -> startSimulation()
            HomeIntent.StopSimulation -> stopSimulation()
        }
    }


    private fun requestRide() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            delay(800)

            val driver = DriverInfo("d1", "Carlos Silva", "Toyota Corolla", "ABC-1234", 4.9)
            val route = mockRoute()


            updateState {
                it.copy(
                    isLoading = false,
                    step = HomeStep.InProgress,
                    ride = RideInfo(driver, etaMinutes = 8, remainingKm = 3.6),
                    driverPosition = route.firstOrNull(),
                    routePolyline = route
                )
            }

            processIntent(HomeIntent.StartSimulation)
        }
    }

    private fun cancelRide() {
        stopSimulation()
        updateState {
            it.copy(
                step = HomeStep.Idle,
                ride = null,
                driverPosition = null,
                routePolyline = emptyList()
            )
        }
    }


    private fun simulateArrival() {
        stopSimulation()
        updateState {
            it.copy(
                step = HomeStep.Idle,
                ride = null,
                driverPosition = null,
                routePolyline = emptyList()
            )
        }
        sendEffect(HomeEffect.NavigateToConfirmation)
    }


    private fun startSimulation() {
        if (simulationJob != null) return
        simulationJob = viewModelScope.launch {
            val poly = state.value.routePolyline
            if (poly.isEmpty()) return@launch


            var idx = 0
            while (idx < poly.size) {
                updateState { s ->
                    s.copy(
                        driverPosition = poly[idx],
                        ride = s.ride?.copy(
                            etaMinutes = computeEta(poly.size - idx),
                            remainingKm = computeRemainingKm(poly.size - idx)
                        )
                    )
                }
                idx++
                delay(1500)
            }
            processIntent(HomeIntent.SimulateArrival)
        }
    }


    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }


    private fun computeEta(pointsRemaining: Int): Int = (pointsRemaining * 1) // simplified
    private fun computeRemainingKm(pointsRemaining: Int): Double = pointsRemaining * 0.5


    private fun mockRoute(): List<LatLng> {
        return listOf(
            LatLng(-23.561414, -46.655881),
            LatLng(-23.562200, -46.656500),
            LatLng(-23.563000, -46.657100),
            LatLng(-23.564000, -46.658000),
            LatLng(-23.565000, -46.659000)
        )
    }
}