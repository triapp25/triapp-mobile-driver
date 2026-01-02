package com.triappdriver.presentation.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triappdriver.BackHandler
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.domain.model.HomeStep
import com.triappdriver.domain.model.PermissionRequest
import com.triappdriver.presentation.feature.profile.ProfileFlowScreen
import com.triappdriver.utils.GeoLocationTracker
import com.triappdriver.utils.MapViewComponent
import com.triappdriver.utils.PermissionsRequester
import com.triappdriver.utils.getLocationTracker
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

// --- CORES DO TEMA ---
val DarkCardColor = Color(0xFF1E1E1E)
val UberGreen = Color(0xFF27C063)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFAAAAAA)
val BackgroundColor = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    onNavigateToRate: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val uiState by viewModel.state.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is HomeEffect.NavigateToConfirmation) {
                onNavigateToRate()
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = true
        )
    )

    LaunchedEffect(uiState.step) {
        // Controla se o sheet está visível (RideOffer) ou expandido (Ativo/Offline)
        if (uiState.step is HomeStep.RideOffer) {
            scaffoldState.bottomSheetState.partialExpand()
        } else {
            scaffoldState.bottomSheetState.expand()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ProfileFlowScreen(
                onBack = { scope.launch { drawerState.close() } },
                onLogout = onLogout
            )
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            containerColor = MaterialTheme.colorScheme.background,
            sheetContainerColor = DarkCardColor,
            sheetContentColor = TextWhite,
            sheetPeekHeight = 160.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),

            // --- CONTEÚDO DA BOTTOM SHEET ---
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    AnimatedContent(
                        targetState = uiState.step,
                        label = "BottomPanelTransition",
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }
                    ) { step ->
                        when (step) {
                            // Dashboard
                            is HomeStep.Offline,
                            is HomeStep.OnlineSearching,
                            is HomeStep.RideOffer -> {
                                EarningsDashboard(
                                    earnings = uiState.earningsToday,
                                    rides = uiState.ridesCount,
                                    hours = uiState.onlineHours,
                                    rating = uiState.rating
                                )
                            }

                            // Painel de Corrida Ativa
                            is HomeStep.NavigatingToPickup,
                            is HomeStep.InProgress,
                            is HomeStep.NearingDestination,
                            is HomeStep.RideCompleted -> {
                                ActiveRideBottomPanel(step, viewModel)
                            }
                        }
                    }
                }
            },

            // --- CONTEÚDO PRINCIPAL (Mapa e Overlays Fixos) ---
            content = {
                Box(modifier = Modifier.fillMaxSize()) {

                    // 1. MAPA (Camada de Fundo)
                    SimulatedMap(
                        driverPosition = uiState.driverPosition,
                        riderPosition = uiState.pickupCoordinate,
                        routePolyline = uiState.routePolyline
                    )

                    // 2. BARRA SUPERIOR (Camada Topo Fixa)
                    HomeTopBar(
                        isOnline = uiState.isOnline,
                        onToggle = { isOnline ->
                            if (isOnline) viewModel.processIntent(HomeIntent.GoOnline)
                            else viewModel.processIntent(HomeIntent.GoOffline)
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                    )

                    // 3. NOTIFICAÇÃO DE NOVA CORRIDA (Camada Topo Flutuante)
                    AnimatedVisibility(
                        visible = uiState.step is HomeStep.RideOffer,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                    ) {
                        (uiState.step as? HomeStep.RideOffer)?.let { offer ->
                            RideRequestNotification(
                                offer = offer,
                                onAccept = { viewModel.processIntent(HomeIntent.AcceptRide) },
                                onDecline = { viewModel.processIntent(HomeIntent.RejectRide) }
                            )
                        }
                    }
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// COMPONENTES UI
// -------------------------------------------------------------------------

@Composable
fun HomeTopBar(
    isOnline: Boolean,
    onToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão Menu
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCardColor.copy(alpha = 0.95f))
                .clickable { onMenuClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextWhite)
        }

        // Switch Online/Offline
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = TextWhite),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                )
                Switch(
                    checked = isOnline,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = UberGreen,
                        checkedTrackColor = Color.Black,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }
    }
}

@Composable
fun RideRequestNotification(
    offer: HomeStep.RideOffer,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    totalTimeSeconds: Int = 10
) {

    val progress = remember { Animatable(1f) }
    var remainingTime by remember { mutableStateOf(totalTimeSeconds) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = totalTimeSeconds * 1000,
                easing = LinearEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        onDecline()
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color.Red,
                trackColor = Color.DarkGray
            )

            Column(Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Nova solicitação", color = TextWhite, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("10s para responder", color = TextGray, fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Passageiro
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(Color.Gray, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            offer.passengerName,
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Estrelas
                            repeat(5) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.height(IntrinsicSize.Min)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Navigation,
                            null,
                            tint = TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Box(Modifier.width(1.dp).weight(1f).background(Color.Gray))
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column {
                            Text("Pegar", color = TextGray, fontSize = 12.sp)
                            Text(
                                offer.pickupAddress,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                offer.distanceToPickup + " faltantes",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Column {
                            Text("Destino", color = TextGray, fontSize = 12.sp)
                            Text(
                                offer.destinationAddress,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Valores lado a lado
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.weight(1f).background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Valor", color = TextGray, fontSize = 12.sp)
                            Text(
                                offer.estimatedFare,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        Modifier.weight(1f).background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Duração", color = TextGray, fontSize = 12.sp)
                            Text(offer.eta, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botões
                Row(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) { Text("Recusar") }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TextWhite,
                            contentColor = Color.Black
                        )
                    ) { Text("Aceitar") }
                }
            }
        }
    }
}

@Composable
fun EarningsDashboard(
    earnings: String,
    rides: Int,
    hours: String,
    rating: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text("Ganhos Hoje", color = TextGray, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        // Card Total Branco
        Card(
            colors = CardDefaults.cardColors(containerColor = TextWhite),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        earnings,
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grid Stats (3 colunas)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardStatItem(Icons.Default.FlashOn, "$rides", "Corridas", Modifier.weight(1f))
            DashboardStatItem(Icons.Outlined.Timer, hours, "Online", Modifier.weight(1f))
            DashboardStatItem(Icons.Default.Star, rating, "Avaliação", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Mock Weekly Summary (para preencher espaço se expandir)
        // Text("Resumo da Semana", color = TextWhite, fontWeight = FontWeight.Bold)
        // Spacer(Modifier.height(8.dp))
        // Box(
        //     Modifier.fillMaxWidth().height(100.dp)
        //         .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
        // )
    }
}

@Composable
fun DashboardStatItem(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(icon, contentDescription = null, tint = TextWhite)
        Spacer(Modifier.height(8.dp))
        Text(value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun ActiveRideBottomPanel(step: HomeStep, viewModel: HomeViewModel) {
    // Obtém o estado atualizado do ViewModel
    val uiState by viewModel.state.collectAsState()

    // EXTRAÇÃO DE DADOS DINÂMICOS DO VIEWMODEL
    val passengerName = uiState.currentRiderName.orEmpty()
    val pickupAddress = uiState.currentPickupAddress.orEmpty()
    val destinationAddress = uiState.currentDestinationAddress.orEmpty()
    val fare = uiState.currentFare
    val passengerNote = uiState.currentPassengerNote

    // Cálculo de tempo e distância (vindos do Mapbox via ViewModel)
    val etaMinutes = uiState.etaMinutes
    val distanceMeters = uiState.distanceMeters

    // Formatação da Distância
    val distanceKm = if (distanceMeters > 1000) {
        "${(distanceMeters / 1000.0).roundToInt()} km"
    } else if (distanceMeters > 0) {
        "${distanceMeters} m"
    } else {
        "Calculando..."
    }
    val timeText = if (etaMinutes > 0) "$etaMinutes min" else "Calculando..."


    // Lógica do Step e Ações
    val statusText: String
    val actionButtonLabel: String
    val actionIntent: HomeIntent
    val showDropoff: Boolean
    val showFare: Boolean

    when (step) {
        is HomeStep.NavigatingToPickup -> {
            statusText = "Em rota para buscar o passageiro"
            actionButtonLabel = "Cheguei ao local de embarque"
            actionIntent = HomeIntent.ArrivedAtPickup
            showDropoff = true
            showFare = false
        }

        is HomeStep.InProgress -> {
            statusText = "Viagem em andamento para o destino"
            actionButtonLabel = "Finalizar Corrida"
            actionIntent = HomeIntent.EndRide
            showDropoff = true
            showFare = true
        }

        is HomeStep.RideCompleted -> {
            statusText = "Corrida finalizada"
            actionButtonLabel = "Ver Resumo e Avaliar"
            actionIntent = HomeIntent.EndRide
            showDropoff = true
            showFare = true
        }

        is HomeStep.NearingDestination -> {
            statusText = "Próximo ao destino"
            actionButtonLabel = "Finalizar Corrida"
            actionIntent = HomeIntent.EndRide
            showDropoff = true
            showFare = true
        }

        else -> return
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier.width(40.dp).height(4.dp).background(Color.Gray, CircleShape)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))

        // Status Card flutuante (Com ETA e Distância dinâmicos)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Navigation, null, tint = Color.Blue)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(statusText, color = TextWhite, fontWeight = FontWeight.Bold)
                // VALOR DINÂMICO DE TEMPO E DISTÂNCIA
                Text(
                    text = "$timeText ($distanceKm faltantes)",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Passageiro Card (Nome e Rating Dinâmicos)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = TextWhite, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        passengerName,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text("${uiState.rating} ★", color = TextGray)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Trip Details (Endereços Dinâmicos)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                // Pickup
                Row {
                    Icon(
                        Icons.Default.Navigation,
                        null,
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Pegar", color = TextGray, fontSize = 12.sp)
                        Text(pickupAddress, color = TextWhite, fontWeight = FontWeight.Bold)

                        // NOTA DO PASSAGEIRO: Mostra apenas ao buscar e se houver nota
                        if (step is HomeStep.NavigatingToPickup && !passengerNote.isNullOrEmpty()) {
                            Text(
                                "Nota: $passengerNote",
                                color = Color.Yellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Dropoff
                if (showDropoff) {
                    Spacer(Modifier.height(16.dp))
                    Row {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Destino", color = TextGray, fontSize = 12.sp)
                            Text(
                                destinationAddress,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Fare (Apenas se showFare for true)
        if (showFare) {
            Card(colors = CardDefaults.cardColors(containerColor = TextWhite)) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ganho da corrida", color = Color.Black)
                    Text(fare, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Garante espaço mesmo sem o card do Fare
        Spacer(Modifier.height(12.dp))


        // BOTÃO DE AÇÃO DINÂMICA
        Button(
            onClick = { viewModel.processIntent(actionIntent) },
            // Cor do botão: UberGreen se estiver em progresso, senão branco padrão
            colors = ButtonDefaults.buttonColors(
                containerColor = if (step is HomeStep.InProgress) UberGreen else TextWhite,
                contentColor = if (step is HomeStep.InProgress) TextWhite else Color.Black
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(actionButtonLabel, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------
// HELPERS
// -------------------------------------------------------------------------


@Composable
fun SimulatedMap(
    driverPosition: Coordinate?,
    riderPosition: Coordinate?,
    routePolyline: List<Coordinate>? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        driverPosition?.let { coord ->
            MapViewComponent(
                modifier = Modifier.fillMaxSize(),
                coordinate = driverPosition,
                routePolyline = routePolyline,
                riderPosition = riderPosition
            )
        } ?: run {
            // Feedback visual enquanto a primeira posição não chega
            Text("Aguardando sinal de GPS...", Modifier.align(Alignment.Center))
        }
    }
}