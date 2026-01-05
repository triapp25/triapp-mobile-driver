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
    private val getRatingUseCase: GetRatingLastUseCase,
    private val geoLocationTracker: GeoLocationTracker,
    private val mapboxRepository: MapboxSearchRepository,
    private val appPreferences: AppPreferences,
    private val taxiUseCase: TaxiUseCase,
    initialState: HomeDomainModel = HomeDomainModel()
) : BaseViewModel<HomeDomainModel, HomeIntent, HomeEffect>(initialState) {

    // Job para controlar o listener do Firestore
    private var firestoreListenerJob: Job? = null
    private var currentTripId: String? = null


    init {
        appPreferences.driverActive(false)
        loadInitialData()
        restoreSession()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            geoLocationTracker.coordinate.collect { loc ->
                if (loc != null) {
                    println("📍 NOVA loc: $loc")
                    val polyline = state.value.routePolyline
                    val driverPos = Coordinate(loc.latitude, loc.longitude)

                    if (polyline.isNotEmpty()) {
                        val metrics = RouteMetricsCalculator.calculate(
                            polyline = polyline,
                            currentPosition = driverPos
                        )

                        updateState {
                            it.copy(
                                etaMinutes = metrics.etaMinutes,
                                distanceMeters = metrics.remainingDistanceMeters.toInt(),
                                routeProgress = metrics.progress,
                                driverPosition = driverPos
                            )
                        }

                        // Chegada automática
                        if (metrics.remainingDistanceMeters < 1000) {
                            processIntent(HomeIntent.AlmostArrived)
                        }
                    } else {
                        updateState { s ->
                            s.copy(
                                driverPosition = driverPos
                            )
                        }
                    }
                }
            }
        }
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            // --- Fluxo Inicial ---
            HomeIntent.GoOnline -> goOnline()
            HomeIntent.GoOffline -> goOffline()

            // --- Ações do Motorista (Atualizam o Firestore) ---
            HomeIntent.AcceptRide -> acceptRide()
            HomeIntent.RejectRide -> rejectRide()
            HomeIntent.ArrivedAtPickup, HomeIntent.StartRide -> updateRideStatus("ONGOING")
            HomeIntent.EndRide -> updateRideStatus("COMPLETED")

            // --- Outros ---
            HomeIntent.AcceptEarlyRide -> { /* Lógica para próxima corrida em fila */
            }

            // Intents de simulação removidos (StartSimulation, SimulateArrival, etc)
            else -> {}
        }
    }

    // -----------------------------------------------------
    // ONLINE / OFFLINE
    // -----------------------------------------------------
    private fun goOnline() {
        appPreferences.driverActive(true)
        updateState { it.copy(step = OnlineSearching, isOnline = true) }
        viewModelScope.launch {
            taxiUseCase.enabledOnline().collect { taxiState ->
                handleTaxiState(taxiState)
            }
        }
    }

    private fun goOffline() {
        stopFirestoreListener()
        appPreferences.driverCurrentTrip("")
        updateState { it.copy(step = Offline, isOnline = false) }
    }

    // -----------------------------------------------------
    // RIDE ACTIONS & FIRESTORE UPDATES
    // -----------------------------------------------------

    private fun acceptRide() {
        // 1. Atualiza o status no banco para ACCEPTED
        updateRideStatus("ACCEPTED")

        // 2. Começa a ouvir o documento da corrida para reagir a mudanças e pegar dados do passageiro
        startFirestoreListener()
    }

    private fun rejectRide() {
        // Opcional: Avisar backend que rejeitou
        updateRideStatus("REJECTED")
        updateState { it.copy(step = OnlineSearching) }
    }

    /**
     * Atualiza o status no Firestore.
     */
    private fun updateRideStatus(newStatus: String) {
        viewModelScope.launch {
            try {

                taxiUseCase.updateTripStatus(
                    currentTripId.orEmpty(), newStatus,
                    LocationDTO(
                        lat = state.value.driverPosition?.latitude ?: 0.0,
                        lng = state.value.driverPosition?.longitude ?: 0.0,
                        address = state.value.currentDestinationAddress.orEmpty()
                    ),
                    state.value.etaMinutes,
                    state.value.distanceMeters
                )
            } catch (e: Exception) {
                // Tratar erro de rede (ex: exibir Snackbar)
                e.printStackTrace()
            }
        }
    }

    // -----------------------------------------------------
    // FIRESTORE LISTENER (REALTIME UPDATES)
    // -----------------------------------------------------

    private fun startFirestoreListener() {
        if (firestoreListenerJob?.isActive == true) return

        firestoreListenerJob = viewModelScope.launch {
            taxiUseCase.startTaxiFlow(currentTripId.orEmpty())
                .collect { taxiState ->
                    handleTaxiState(taxiState)
                }
        }
    }

    private fun stopFirestoreListener() {
        firestoreListenerJob?.cancel()
        firestoreListenerJob = null
    }


    /**
     * Mapeia o estado vindo do Firestore (TaxiState) para o estado da UI (HomeStep)
     */
    private fun handleTaxiState(taxiState: TaxiState) {
        when (taxiState) {
            is TaxiState.Online -> {
                currentTripId = taxiState.rider.tripId

                if (currentTripId != null) {
                    val rider: RiderFirebaseDTO = taxiState.rider

                    val passengerName = rider.name.orEmpty()
                    val passengerRating = rider.rating?.toString().orEmpty()
                    val fare = rider.fare.orEmpty()
                    val etaMin = rider.etaMin.orEmpty()
                    val distance = rider.distance.orEmpty()
                    val pickupAddress = rider.pickup.name.orEmpty()
                    val destinationAddress = rider.rider.location.name.orEmpty()

                    val pickupCoord = Coordinate(rider.pickup.lat, rider.pickup.lng)
                    val destinationCoord =
                        Coordinate(rider.rider.location.lat, rider.rider.location.lng)

                    // 1. SALVAR DADOS NO ESTADO DO VIEWMODEL (Incluindo Coordenadas)
                    updateState {
                        it.copy(
                            currentRiderName = passengerName,
                            currentRating = passengerRating,
                            currentPickupAddress = pickupAddress,
                            currentDestinationAddress = destinationAddress,
                            currentPassengerNote = rider.rider.note,
                            currentFare = fare, // Definir valor real se estiver no DTO
                            pickupCoordinate = pickupCoord, // SALVANDO COORDENADAS
                            destinationCoordinate = destinationCoord, // SALVANDO COORDENADAS
                            step = RideOffer(
                                passengerName = passengerName,
                                pickupAddress = pickupAddress,
                                destinationAddress = destinationAddress,
                                distanceToPickup = "$distance Km",
                                estimatedFare = fare,
                                eta = "$etaMin min",
                                passengerNote = rider.rider.note
                            )
                        )
                    }
                }
            }

            is TaxiState.Update -> {
                val status = taxiState.status

                // Obtém os dados salvos no estado (necessário para persistência da UI)
                val savedRiderName = state.value.currentRiderName.orEmpty()
                val savedPickupAddress = state.value.currentPickupAddress.orEmpty()
                val savedDestinationAddress = state.value.currentDestinationAddress.orEmpty()

                // Define o próximo passo da UI baseado no status do banco
                val nextStep = when (status) {
                    "ACCEPTED" -> {
                        // O rastreamento de métricas já foi iniciado em acceptRide()
                        viewModelScope.launch {
                            val routeResult = state.value.driverPosition?.let {
                                mapboxRepository.getRoutePolyline(
                                    it,
                                    state.value.pickupCoordinate!!
                                )
                            }
                            val polyline = routeResult?.second.orEmpty()
                            updateState {
                                it.copy(routePolyline = polyline, targetCoordinate = state.value.pickupCoordinate)
                            }
                        }

                        appPreferences.driverCurrentTrip(currentTripId.orEmpty())
                        NavigatingToPickup(
                            passengerName = savedRiderName,
                            pickupAddress = savedPickupAddress
                        )
                    }

                    "ARRIVED" -> {
                        // Chegou ao pickup, agora o rastreamento deve mudar de alvo (se não o fez automaticamente)
                        NavigatingToPickup( // Mantém na tela de pickup, mas com status visual de ARRIVED
                            passengerName = savedRiderName,
                            pickupAddress = "Aguardando embarque em $savedPickupAddress"
                        )
                    }

                    "ONGOING" -> {
                        viewModelScope.launch {
                            val routeResult = state.value.driverPosition?.let {
                                mapboxRepository.getRoutePolyline(
                                    it,
                                    state.value.destinationCoordinate!!
                                )
                            }
                            val polyline = routeResult?.second.orEmpty()
                            updateState {
                                it.copy(routePolyline = polyline, targetCoordinate = state.value.destinationCoordinate)
                            }
                        }

                        InProgress(
                            passengerName = savedRiderName,
                            destinationAddress = savedDestinationAddress,
                            timeRemainingMinutes = state.value.etaMinutes
                        )
                    }

                    "COMPLETED" -> {
                        appPreferences.driverCurrentTrip("")
                        RideCompleted(savedRiderName)
                    }

                    else -> state.value.step
                }

                updateState { it.copy(step = nextStep) }
            }

            is TaxiState.Completed -> {
                stopFirestoreListener()

                val savedRiderName = state.value.currentRiderName.orEmpty()

                updateState {
                    it.copy(
                        step = RideCompleted(savedRiderName),
                        // Limpar dados da viagem
                        currentRiderName = null,
                        currentDestinationAddress = null,
                        currentPickupAddress = null
                    )
                }
                sendEffect(HomeEffect.NavigateToConfirmation)
            }

            is TaxiState.Error -> {
                println("Erro no listener: ${taxiState.message}")
            }

            TaxiState.Idle -> {}
            is TaxiState.Started -> {}
        }
    }

    // -----------------------------------------------------
    // RESTORE SESSION
    // -----------------------------------------------------
    private fun restoreSession() {
        viewModelScope.launch {
            appPreferences.isDriverCurrentTrip().collect { driverCurrentTrip ->
                if (driverCurrentTrip.isBlank()) return@collect
                // Simplesmente volta para o modo online, o listener de rides cuida do resto
                currentTripId = driverCurrentTrip
                updateState { it.copy(isOnline = true) }
                appPreferences.driverActive(true)
                // Se houver tripId ativo salvo, iniciar listeners
                startFirestoreListener()
            }
        }
    }
}