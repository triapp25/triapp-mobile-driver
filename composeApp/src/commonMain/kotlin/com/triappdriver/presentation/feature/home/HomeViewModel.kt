package com.triappdriver.presentation.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.triappdriver.data.dtos.RiderFirebaseDTO
import com.triappdriver.data.repository.MapboxSearchRepository
import com.triappdriver.domain.model.*
import com.triappdriver.domain.model.HomeStep.*
import com.triappdriver.domain.usecase.GetRatingLastUseCase
import com.triappdriver.domain.usecase.TaxiUseCase
import com.triappdriver.local.AppPreferences
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.LocationRepository
import dev.icerock.moko.geo.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(
    private val getRatingUseCase: GetRatingLastUseCase,
    private val locationRepository: LocationRepository,
    private val mapboxRepository: MapboxSearchRepository,
    private val appPreferences: AppPreferences,
    private val taxiUseCase: TaxiUseCase,
    initialState: HomeDomainModel = HomeDomainModel()
) : BaseViewModel<HomeDomainModel, HomeIntent, HomeEffect>(initialState) {

    // Job para controlar o listener do Firestore
    private var firestoreListenerJob: Job? = null
    private var currentTripId: String? = null

    // Job para controlar o rastreamento de métricas em tempo real (ETA/Distância)
    private var rideMetricsJob: Job? = null

    init {
        loadInitialData()
        restoreSession()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val permission = locationRepository.requestLocationPermission()

            if (permission) {
                val loc = locationRepository.getCurrentLocation()
                if (loc != null) {
                    updateState { s ->
                        s.copy(
                            driverPosition = Coordinate(loc.first, loc.second)
                        )
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

            HomeIntent.ArrivedAtPickup -> updateRideStatus("ONGOING")
            HomeIntent.StartRide -> updateRideStatus("ONGOING")
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
        updateState { it.copy(step = OnlineSearching, isOnline = true) }
        viewModelScope.launch {
            taxiUseCase.enabledOnline().collect { taxiState ->
                handleTaxiState(taxiState)
            }
        }
    }

    private fun goOffline() {
        stopFirestoreListener()
        stopRideMetricsTracking() // NOVO: Para o rastreamento
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

        // 3. NOVO: Inicia o rastreamento de métricas em tempo real (ETA/Distância)
        startRideMetricsTracking()
    }

    private fun rejectRide() {
        // Opcional: Avisar backend que rejeitou
        updateState { it.copy(step = OnlineSearching) }
    }

    /**
     * Atualiza o status no Firestore.
     */
    private fun updateRideStatus(newStatus: String) {
        viewModelScope.launch {
            try {
                taxiUseCase.updateTripStatus(currentTripId.orEmpty(), newStatus)
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

    // -----------------------------------------------------
    // MÉTRICAS DE ROTA E RASTREAMENTO (LÓGICA DO MOTORISTA)
    // -----------------------------------------------------

    /**
     * Inicia o rastreamento em tempo real da localização do motorista
     * e calcula a distância/ETA até o ponto de interesse (Pickup ou Dropoff).
     */
    private fun startRideMetricsTracking() {
        rideMetricsJob?.cancel() // Cancela o job anterior

        rideMetricsJob = viewModelScope.launch {
            // Ouve a localização do motorista em tempo real

            // Determina a coordenada alvo com base no HomeStep atual
            val targetLatLng: Coordinate? = when (state.value.step) {
                is HomeStep.NavigatingToPickup -> state.value.pickupCoordinate
                is HomeStep.InProgress -> state.value.destinationCoordinate
                else -> null
            }

            if (targetLatLng != null) {
                try {
                    val origin = state.value.driverPosition
                    val destination = targetLatLng

                    // Calcula a rota (polyline, duração, distância)
                    // NOTA: mapboxRepository.getRoutePolyline deve ser capaz de receber LatLng
                    val routeResult = mapboxRepository.getRoutePolyline(origin!!, destination)

                    val distanceMeters = routeResult?.distance?.toInt() ?: 0
                    // Duration está em segundos, converter para minutos
                    val etaMinutes = (routeResult?.duration?.div(60.0))?.toInt() ?: 0

                    // Atualiza o estado da UI com as métricas e a polyline
                    updateState { s ->
                        s.copy(
                            etaMinutes = etaMinutes.coerceAtLeast(1),
                            distanceMeters = distanceMeters.coerceAtLeast(0),
                        )
                    }

                    // Lógica de Chegada (Simulação de clique no botão "Chegou")
                    // Se estiver indo buscar o passageiro e estiver muito perto
                    if (state.value.step is HomeStep.NavigatingToPickup && distanceMeters < 50) {
                        processIntent(HomeIntent.ArrivedAtPickup)
                    }

                } catch (e: Exception) {
                    println("Erro ao calcular rota: ${e.message}")
                }
            } else {
                // Se não houver alvo válido (ex: Corrida cancelada), para o rastreamento
                stopRideMetricsTracking()
            }
        }
    }

    private fun stopRideMetricsTracking() {
        rideMetricsJob?.cancel()
        rideMetricsJob = null
        // Limpar métricas no estado
        updateState { it.copy(etaMinutes = 0, distanceMeters = 0, routePolyline = emptyList()) }
    }


    /**
     * Mapeia o estado vindo do Firestore (TaxiState) para o estado da UI (HomeStep)
     */
    private fun handleTaxiState(taxiState: TaxiState) {
        when (taxiState) {
            is TaxiState.Online -> {
                currentTripId = taxiState.rider.tripId
                val rider: RiderFirebaseDTO = taxiState.rider

                val passengerName = rider.rider.name.orEmpty()
                val pickupAddress = rider.pickup.name.orEmpty()
                val destinationAddress = rider.rider.location.name.orEmpty()

                val pickupCoord = Coordinate(rider.pickup.lat, rider.pickup.lng)
                val destinationCoord = Coordinate(rider.rider.location.lat, rider.rider.location.lng)

                // 1. SALVAR DADOS NO ESTADO DO VIEWMODEL (Incluindo Coordenadas)
                updateState {
                    it.copy(
                        currentRiderName = passengerName,
                        currentPickupAddress = pickupAddress,
                        currentDestinationAddress = destinationAddress,
                        currentPassengerNote = rider.rider.note,
                        currentFare = "R$ 0,00", // Definir valor real se estiver no DTO
                        pickupCoordinate = pickupCoord, // SALVANDO COORDENADAS
                        destinationCoordinate = destinationCoord, // SALVANDO COORDENADAS
                        step = RideOffer(
                            passengerName = passengerName,
                            pickupAddress = pickupAddress,
                            destinationAddress = destinationAddress,
                            distanceToPickup = "calculando...",
                            estimatedFare = "R$ --,--",
                            eta = "-- min",
                            passengerNote = rider.rider.note
                        )
                    )
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

                    "IN_PROGRESS" -> {
                        // Viagem iniciada. O rastreamento de métricas agora foca no DESTINO.
                        // O `startRideMetricsTracking` já faz isso automaticamente ao checar o `state.value.step`.
                        InProgress(
                            passengerName = savedRiderName,
                            destinationAddress = savedDestinationAddress,
                            timeRemainingMinutes = state.value.etaMinutes
                        )
                    }

                    "COMPLETED" -> {
                        RideCompleted(savedRiderName)
                    }

                    else -> state.value.step
                }

                updateState { it.copy(step = nextStep) }
            }

            is TaxiState.Completed -> {
                stopFirestoreListener()
                stopRideMetricsTracking() // NOVO: Para o rastreamento

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
            appPreferences.isDriverActive().collect { isOnline ->
                if (isOnline) {
                    // Simplesmente volta para o modo online, o listener de rides cuida do resto
                    updateState { it.copy(isOnline = true) }
                    // Se houver tripId ativo salvo, iniciar listeners
                    // startFirestoreListener()
                }
            }
        }
    }
}