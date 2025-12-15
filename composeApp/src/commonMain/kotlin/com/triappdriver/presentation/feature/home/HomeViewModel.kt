package com.triappdriver.presentation.feature.home

import androidx.lifecycle.viewModelScope
import com.triappdriver.data.repository.MapboxSearchRepository
import com.triappdriver.domain.model.*
import com.triappdriver.domain.model.HomeStep.*
import com.triappdriver.domain.usecase.GetRatingLastUseCase
import com.triappdriver.domain.usecase.TaxiUseCase
import com.triappdriver.local.AppPreferences
import com.triappdriver.presentation.BaseViewModel
import com.triappdriver.utils.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    init {
        restoreSession()
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            // --- Fluxo Inicial ---
            HomeIntent.GoOnline -> goOnline()
            HomeIntent.GoOffline -> goOffline()

            // --- Ações do Motorista (Atualizam o Firestore) ---
            HomeIntent.AcceptRide -> acceptRide()
            HomeIntent.RejectRide -> rejectRide()

            HomeIntent.ArrivedAtPickup -> updateRideStatus("ARRIVED")
            HomeIntent.StartRide -> updateRideStatus("IN_PROGRESS")
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
        updateState { it.copy(step = OnlineSearching) }
    }

    /**
     * Atualiza o status no Firestore.
     * O 'TaxiUseCase' deve ter um método updateTripStatus.
     */
    private fun updateRideStatus(newStatus: String) {
        viewModelScope.launch {
            try {
                // Supondo que você adicionou este método no UseCase conforme refatoração anterior
                taxiUseCase.updateTripStatus(currentTripId, newStatus)
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
            taxiUseCase.startTaxiFlow(currentTripId)
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
                updateState {
                    it.copy(
                        step = RideOffer(
                            passengerName = taxiState.rider.name.orEmpty(), // Viria do backend na oferta
                            pickupAddress = taxiState.rider.pickup.name.orEmpty(),
                            destinationAddress = taxiState.rider.rider.location.name.orEmpty(),
                            distanceToPickup = "calculando...",
                            estimatedFare = "R$ --,--",
                            eta = "-- min"
                        )
                    )
                }
            }

            is TaxiState.Update -> {
                val driverData = taxiState.driverDTO // Dados vindos do DTO
                val status =
                    taxiState.status     // Status vindo do Firestore (ACCEPTED, ARRIVED, etc)

                // Define o próximo passo da UI baseado no status do banco
                val nextStep = when (status) {
                    "ACCEPTED" -> {
                        NavigatingToPickup(
                            passengerName = "Passageiro (Via Banco)",
                            pickupAddress = "Verificar coordenadas no DTO"
                        )
                    }

                    "ARRIVED" -> {
                        // Mantém na tela de pickup, mas muda o texto ou estado interno se necessário
                        // Ou se tiver um step específico: HomeStep.WaitingForPassenger
                        NavigatingToPickup(
                            passengerName = "Passageiro",
                            pickupAddress = "Aguardando embarque..."
                        )
                    }

                    "IN_PROGRESS" -> {
                        InProgress(
                            passengerName = "Passageiro",
                            destinationAddress = "Destino Final",
                            timeRemainingMinutes = 0 // Calcular real com MapBox
                        )
                    }

                    "COMPLETED" -> {
                        // Será tratado no bloco TaxiState.Completed abaixo ou aqui
                        RideCompleted("Passageiro")
                    }

                    else -> state.value.step // Mantém estado atual se status desconhecido
                }

                updateState { it.copy(step = nextStep) }
            }

            is TaxiState.Completed -> {
                stopFirestoreListener()
                updateState { it.copy(step = RideCompleted("Passageiro")) }
                // Navegar para rating ou resetar
                sendEffect(HomeEffect.NavigateToConfirmation)
            }

            is TaxiState.Error -> {
                // Lidar com erro
                println("Erro no listener: ${taxiState.message}")
            }

            TaxiState.Idle -> TODO()
            is TaxiState.Started -> TODO()
        }
    }

    // -----------------------------------------------------
    // RESTORE SESSION
    // -----------------------------------------------------
    private fun restoreSession() {
        viewModelScope.launch {
            val isDriverActive = appPreferences.isDriverActive().collect {
                if (it) {
                    updateState { it.copy(isOnline = true) }
                    startFirestoreListener()
                }
            }
        }
    }
}