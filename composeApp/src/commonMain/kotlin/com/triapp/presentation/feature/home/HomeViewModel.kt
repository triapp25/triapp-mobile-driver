package com.triapp.presentation.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.triapp.data.repository.MapboxSearchRepository
import com.triapp.domain.model.*
import com.triapp.domain.usecase.GetRatingLastUseCase
import com.triapp.local.AppPreferences
import com.triapp.presentation.BaseViewModel
import com.triapp.utils.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getRatingUseCase: GetRatingLastUseCase,
    private val locationRepository: LocationRepository,
    private val mapboxRepository: MapboxSearchRepository,
    private val appPreferences: AppPreferences,
    initialState: HomeDomainModel = HomeDomainModel()
) : BaseViewModel<HomeDomainModel, HomeIntent, HomeEffect>(initialState) {

    private var simulationJob: Job? = null

    init {
        loadInitialData()
        restoreSession()
        if (state.value.step == HomeStep.Initial) checkPendingRatings()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. Carregar Cartão Padrão (Simulado)
            val defaultCard = WalletDomainModel(
                icon = Icons.Default.CreditCard,
                color = Color.Black,
                title = "Visa",
                subtitle = "**** 4242",
                extraInfo = "Expira 12/28",
                isDefault = true
            )

            // 2. Buscar Localização Real
            val permission = locationRepository.requestLocationPermission()

            // Endereço padrão caso não ache
            var currentAddr = "Av. Paulista, 1578"
            var currentLatLng: LatLng? = null

            if (permission) {
                val loc = locationRepository.getCurrentLocation()
                if (loc != null) {
                    // SIMULAÇÃO DE REVERSE GEOCODING (API)
                    // Na prática, você enviaria loc.first/loc.second para a API do Mapbox
                    // e ela retornaria "Rua Haddock Lobo, 595".
                    // Por enquanto, vamos simular que o GPS retornou este endereço:
                    currentAddr = "Rua Haddock Lobo, 595"
                    currentLatLng = LatLng(loc.first, loc.second)
                }
            }

            updateState {
                it.copy(
                    defaultWallet = defaultCard,
                    pickup = currentAddr, // Agora mostra o endereço, não "Minha loc"
                    pickupAddress = currentLatLng?.let { latLng ->
                        SearchResult("0", currentAddr, "São Paulo, SP", latLng.lat, latLng.lng)
                    }
                )
            }
        }
    }

    // --- PERSISTÊNCIA DE ESTADO ---
    private fun restoreSession() {
        viewModelScope.launch {
            // Verifica se existe flag de corrida ativa no disco
            val hasActiveRide = appPreferences.observeSignUpDraft().toString()
                .contains("ACTIVE_RIDE") // Simplificação

            if (hasActiveRide) {
                // Recupera dados do disco/API
                val driver = DriverInfo("d1", "Carlos Silva", "Toyota Corolla", "ABC-1234", "4.9")
                val route = mockRoute()

                updateState {
                    it.copy(
                        step = HomeStep.InProgress,
                        driver = driver,
                        ride = RideInfo(driver, 12, 5.0),
                        driverPosition = route.first(),
                        routePolyline = route,
                        tripStatusMessage = "Viagem em andamento"
                    )
                }
                startSimulation()
            }
        }
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            // Navegação Básica
            is HomeIntent.EnterOrigin -> updateState { it.copy(step = HomeStep.DestinationSearch) }
            is HomeIntent.EnterDestination -> updateState { it.copy(step = HomeStep.DestinationSearch) }

            // Edição de Endereços (Volta para busca)
            is HomeIntent.EditPickup -> updateState { it.copy(step = HomeStep.DestinationSearch) } // Poderia adicionar flag isEditingPickup
            is HomeIntent.EditDropoff -> updateState { it.copy(step = HomeStep.DestinationSearch) }

            // Carteira
            is HomeIntent.OpenWallet -> updateState { it.copy(step = HomeStep.PaymentSelection) }
            is HomeIntent.CloseWallet -> updateState { it.copy(step = HomeStep.RideSelection) }

            // Busca e Seleção
            is HomeIntent.SearchAddress -> searchAddress(intent.query)
            is HomeIntent.SelectAddress -> selectAddress(intent.result, intent.isDestination)
            is HomeIntent.SelectOption -> updateState { it.copy(selectedOption = intent.option) }

            // Fluxo da Corrida
            HomeIntent.ConfirmRequest -> requestRide()
            HomeIntent.CancelRide -> cancelRide()

            // Simulação Interna
            HomeIntent.StartSimulation -> startSimulation()
            HomeIntent.SimulateArrival -> simulateArrival()
            else -> {}
        }
    }

    private fun searchAddress(query: String) {
        viewModelScope.launch {
            updateState { it.copy(isSearchingLocation = true) }
            val results = mapboxRepository.searchPlaces(query)
            updateState { it.copy(searchResults = results, isSearchingLocation = false) }
        }
    }

    private fun selectAddress(result: SearchResult, isDestination: Boolean) {
        if (isDestination) {
            updateState { it.copy(dropoff = result.name, dropoffAddress = result) }
        } else {
            updateState { it.copy(pickup = result.name, pickupAddress = result) }
        }

        // Se selecionou o destino, vai para seleção de carro
        // (Assumindo que origem já foi pega pelo GPS ou selecionada antes)
        if (isDestination) {
            fetchRideOptions()
        }
    }

    private fun fetchRideOptions() {
        viewModelScope.launch {
            updateState { it.copy(step = HomeStep.RideSelection, isLoadingRide = true) }
            delay(1500) // Simula API de cálculo de rota

            val options = listOf(
                RideOption("1", "Economy", "4 min", "R$ 15.90", Icons.Default.FlashOn),
                RideOption("2", "Comfort", "3 min", "R$ 22.50", Icons.Default.Star),
                RideOption("3", "Black", "5 min", "R$ 35.00", Icons.Default.DirectionsCar)
            )

            updateState {
                it.copy(
                    availableOptions = options,
                    selectedOption = options.first(), // Seleciona o primeiro
                    isLoadingRide = false
                )
            }
        }
    }

    private fun requestRide() {
        viewModelScope.launch {
            // 1. Procurando
            updateState { it.copy(step = HomeStep.Searching) }

            // Salvar persistência (Simulado)
            // appPreferences.putString("ride_status", "ACTIVE_RIDE")

            delay(3000) // Simula WebSocket encontrando motorista

            // 2. Motorista Encontrado
            val driver = DriverInfo("d1", "Carlos Silva", "Honda Civic", "ABC-1234", "4.9")
            updateState {
                it.copy(step = HomeStep.DriverFound, driver = driver, etaMessage = "3 min")
            }
            delay(3000)

            // 3. Viagem Iniciada
            val route = mockRoute()
            updateState {
                it.copy(
                    step = HomeStep.InProgress,
                    ride = RideInfo(driver, 15, 5.2),
                    driverPosition = route.first(),
                    routePolyline = route,
                    tripStatusMessage = "A caminho do destino"
                )
            }
            processIntent(HomeIntent.StartSimulation)
        }
    }

    private fun startSimulation() {
        if (simulationJob != null) return
        simulationJob = viewModelScope.launch {
            val poly = state.value.routePolyline
            if (poly.isEmpty()) return@launch

            var idx = 0
            while (idx < poly.size && state.value.step == HomeStep.InProgress) {
                val progress = idx.toFloat() / poly.size.toFloat()
                updateState { s ->
                    val remaining = poly.size - idx
                    s.copy(
                        driverPosition = poly[idx],
                        routeProgress = progress,
                        etaMessage = "${(remaining * 0.5).toInt().coerceAtLeast(1)} min"
                    )
                }
                idx++
                delay(1000)
            }
            processIntent(HomeIntent.SimulateArrival)
        }
    }

    fun checkPendingRatings() {
        viewModelScope.launch {
            getRatingUseCase.invoke().onSuccess { lastRating ->
                if (lastRating != null && !lastRating.isRated) {
                    updateState {
                        it.copy(pendingRating = lastRating)
                    }
                }
            }
        }
    }

    private fun simulateArrival() {
        stopSimulation()
        sendEffect(HomeEffect.NavigateToConfirmation)
    }

    private fun cancelRide() {
        stopSimulation()
        updateState {
            it.copy(
                step = HomeStep.Initial,
                ride = null,
                driverPosition = null,
                routePolyline = emptyList(),
                dropoff = "",
                dropoffAddress = null
            )
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun mockRoute(): List<LatLng> {
        return listOf(
            LatLng(-23.561, -46.655), LatLng(-23.562, -46.656),
            LatLng(-23.563, -46.657), LatLng(-23.564, -46.658),
            LatLng(-23.565, -46.659), LatLng(-23.566, -46.660)
        )
    }
}