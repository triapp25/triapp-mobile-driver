package com.triapp.presentation.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triapp.BackHandler
import com.triapp.TriColors.AccentGreen
import com.triapp.domain.model.*
import com.triapp.presentation.feature.profile.ProfileFlowScreen
import com.triapp.presentation.feature.profile.WalletScreen
import com.triapp.utils.GeoLocationTracker
import com.triapp.utils.MapViewComponent
import com.triapp.utils.PermissionsRequester
import com.triapp.utils.getLocationTracker
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ================== TELA PRINCIPAL ==================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRate: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val uiState by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler {
        when (uiState.step) {
            // Se estiver escolhendo pagamento, volta para seleção de carro
            HomeStep.PaymentSelection -> viewModel.processIntent(HomeIntent.CloseWallet)

            // Se estiver buscando endereço, volta para o início
            HomeStep.DestinationSearch -> viewModel.processIntent(HomeIntent.CancelRide)

            // Se estiver selecionando carro, cancela e volta para o início
            HomeStep.RideSelection -> viewModel.processIntent(HomeIntent.CancelRide)

            // Se estiver buscando motorista ou motorista encontrado, cancela a busca
            HomeStep.Searching, HomeStep.DriverFound -> viewModel.processIntent(HomeIntent.CancelRide)

            HomeStep.InProgress -> { /* Opcional: Minimizar app ou ignorar */ }

            else -> { /* HomeStep.Initial não entra aqui por causa do enabled=false */ }
        }
    }

    // Efeitos de Navegação
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is HomeEffect.NavigateToConfirmation) {
                onNavigateToRate()
            }
        }
    }

    // Scaffold State do BottomSheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = true
        )
    )

    // Controla a expansão do Sheet via Estado da ViewModel
    LaunchedEffect(uiState.step) {
        when (uiState.step) {
            HomeStep.Initial -> scaffoldState.bottomSheetState.partialExpand()
            HomeStep.DestinationSearch, HomeStep.PaymentSelection -> scaffoldState.bottomSheetState.expand()
            HomeStep.RideSelection, HomeStep.Searching -> scaffoldState.bottomSheetState.partialExpand()
            HomeStep.DriverFound, HomeStep.InProgress -> scaffoldState.bottomSheetState.expand()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // Bloqueia swipe lateral
        drawerContent = {
            ProfileFlowScreen(
                onBack = { scope.launch { drawerState.close() } },
                onLogout = onLogout
            )
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            // Cores do Tema
            containerColor = MaterialTheme.colorScheme.background, // Fundo atrás do mapa
            sheetContainerColor = MaterialTheme.colorScheme.surface, // Fundo do BottomSheet
            sheetContentColor = MaterialTheme.colorScheme.onSurface, // Texto padrão

            sheetPeekHeight = 180.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetDragHandle = if (uiState.step == HomeStep.DestinationSearch) null else {
                { BottomSheetDefaults.DragHandle() }
            },
            sheetContent = {
                val isFullScreen = uiState.step == HomeStep.DestinationSearch || uiState.step == HomeStep.PaymentSelection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // <--- IMPORTANTE: Empurra conteúdo para cima da barra do sistema
                        .padding(horizontal = if (isFullScreen) 0.dp else 24.dp)
                        .then(if (isFullScreen) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
                ) {
                    AnimatedContent(
                        targetState = uiState.step,
                        label = "HomeFlow",
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
                    ) { step ->
                        when (step) {
                            HomeStep.Initial -> InitialStateView(
                                currentLocation = uiState.pickup,
                                onOriginClick = { viewModel.processIntent(HomeIntent.EnterOrigin) },
                                onDestinationClick = { viewModel.processIntent(HomeIntent.EnterDestination) }
                            )

                            HomeStep.DestinationSearch -> DestinationSearchView(
                                searchResults = uiState.searchResults,
                                isLoading = uiState.isSearchingLocation,
                                onQueryChange = { viewModel.processIntent(HomeIntent.SearchAddress(it)) },
                                onResultClick = { viewModel.processIntent(HomeIntent.SelectAddress(it, true)) },
                                onBack = { viewModel.processIntent(HomeIntent.CancelRide) }
                            )

                            HomeStep.RideSelection -> RideSelectionView(
                                options = uiState.availableOptions,
                                selectedOption = uiState.selectedOption,
                                defaultWallet = uiState.defaultWallet,
                                pickupAddress = uiState.pickup,
                                dropoffAddress = uiState.dropoff,
                                isLoading = uiState.isLoadingRide,
                                onSelectOption = { viewModel.processIntent(HomeIntent.SelectOption(it)) },
                                onConfirm = { viewModel.processIntent(HomeIntent.ConfirmRequest) },
                                onPaymentClick = { viewModel.processIntent(HomeIntent.OpenWallet) },
                                onEditPickup = { viewModel.processIntent(HomeIntent.EditPickup) },
                                onEditDropoff = { viewModel.processIntent(HomeIntent.EditDropoff) }
                            )

                            HomeStep.PaymentSelection -> {
                                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                                    Box(Modifier.padding(16.dp)) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack, "Back",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.clickable { viewModel.processIntent(HomeIntent.CloseWallet) }
                                        )
                                    }
                                    WalletScreen(
                                        balance = "R$ 0.00",
                                        walletList = emptyList(), // Lista real viria da VM
                                        onAddCard = {},
                                        onDelete = {}
                                    )
                                }
                            }

                            HomeStep.Searching -> SearchingView(
                                onCancel = { viewModel.processIntent(HomeIntent.CancelRide) }
                            )

                            HomeStep.DriverFound -> DriverFoundView(
                                driver = uiState.driver,
                                eta = uiState.etaMessage,
                                onCancel = { viewModel.processIntent(HomeIntent.CancelRide) }
                            )

                            HomeStep.InProgress -> InProgressView(
                                status = uiState.tripStatusMessage,
                                remaining = uiState.etaMessage,
                                progress = uiState.routeProgress,
                                driver = uiState.driver,
                                origin = uiState.pickup,
                                destination = uiState.dropoff,
                                onShareClick = { /* Share */ }
                            )
                        }
                    }
                }
            },
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    SimulatedMap(driverPosition = uiState.driverPosition)

                    if (uiState.step == HomeStep.InProgress) {
                        InProgressMapOverlays(uiState.etaMessage)
                    } else {
                        MenuFloatingButton(
                            modifier = Modifier.padding(top = 48.dp, start = 20.dp).align(Alignment.TopStart),
                            onClick = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        )
    }
}

// ================== VIEWS DE ESTADO (USANDO CORES DO TEMA) ==================

@Composable
fun InitialStateView(
    currentLocation: String,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Espaço entre os campos
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Campo 1: Origem (Visual Igual ao Destino)
        LocationInputRow(
            icon = Icons.Default.MyLocation,
            iconTint = AccentGreen, // Verde para origem
            text = currentLocation,
            textColor = MaterialTheme.colorScheme.primary,
            onClick = { onOriginClick }
        )

        // Campo 2: Destino
        LocationInputRow(
            icon = Icons.Default.Search,
            iconTint = MaterialTheme.colorScheme.primary, // Azul/Primary do tema
            text = "Enter destination",
            textColor = MaterialTheme.colorScheme.secondary, // Cinza pois é placeholder
            onClick = onDestinationClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Atalhos (Home/Work)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShortcutCard(Modifier.weight(1f), "Home", Icons.Default.Home)
            ShortcutCard(Modifier.weight(1f), "Work", Icons.Default.Work)
        }

        // Recentes
        Spacer(modifier = Modifier.height(16.dp))
        RecentItem(Icons.Default.Schedule, "Av. Paulista, 1578", "Bela Vista")
        Spacer(modifier = Modifier.height(12.dp))
        RecentItem(Icons.Default.Star, "Shopping Iguatemi", "Jardim Paulistano")
    }
}

@Composable
fun RecentItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFF2C2C2E), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun ShortcutCard(modifier: Modifier, title: String, icon: ImageVector) {
    Row(
        modifier = modifier
            .height(60.dp)
            .background(Color(0xFF2C2C2E), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LocationInputRow(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)) // Fundo Cinza/SurfaceVariant
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RideSelectionView(
    options: List<RideOption>,
    selectedOption: RideOption?,
    defaultWallet: WalletDomainModel?,
    pickupAddress: String,
    dropoffAddress: String,
    isLoading: Boolean,
    onSelectOption: (RideOption) -> Unit,
    onConfirm: () -> Unit,
    onPaymentClick: () -> Unit,
    onEditPickup: () -> Unit,
    onEditDropoff: () -> Unit
) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            // Pickup
            Row(Modifier.clickable { onEditPickup() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(pickupAddress, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            // Dropoff
            Row(Modifier.clickable { onEditDropoff() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(dropoffAddress, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            options.forEach { ride ->
                RideOptionItem(ride, isSelected = selectedOption?.id == ride.id) { onSelectOption(ride) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pagamento
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onPaymentClick() }.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = defaultWallet?.icon ?: Icons.Default.CreditCard
            val text = defaultWallet?.let { "${it.title} ${it.subtitle}" } ?: "Select Payment"

            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Button(
            onClick = onConfirm,
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirm ride", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DestinationSearchView(
    searchResults: List<SearchResult>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onBack: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.height(16.dp))
            CustomTextField(
                value = searchText,
                onValueChange = { searchText = it; onQueryChange(it) },
                placeholder = "Where to?",
                isHighLighted = true
            )
        }
        if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        LazyColumn { items(searchResults) { SearchResultItem(it) { onResultClick(it) } } }
    }
}

@Composable
fun SearchingView(onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Finding nearby drivers...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) { Text("Cancel") }
    }
}

@Composable
fun DriverFoundView(driver: DriverInfo?, eta: String, onCancel: () -> Unit) {
    if (driver == null) return
    Column(Modifier.padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Driver found!", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Arriving in $eta", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        DriverCard(driver)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) { Text("Cancel") }
    }
}

@Composable
fun InProgressView(
    status: String, remaining: String, progress: Float, driver: DriverInfo?,
    origin: String, destination: String, onShareClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(remaining, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        driver?.let { DriverCard(it) }
        Spacer(Modifier.height(32.dp))
        RouteTimeline(origin, destination)
        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) { Text("Share trip") }
    }
}

// ================== HELPERS ==================

@Composable
fun DriverCard(driver: DriverInfo) {
    Box(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(driver.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("${driver.carModel} • ${driver.plate}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Box(Modifier.background(MaterialTheme.colorScheme.onSurface, CircleShape).size(40.dp), contentAlignment = Alignment.Center) {
                Text(driver.rating, color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RouteTimeline(origin: String, destination: String) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Box(modifier = Modifier.weight(1f).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
            Text(origin, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, maxLines = 1)
            Spacer(Modifier.height(24.dp))
            Text(destination, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, maxLines = 1)
        }
    }
}

@Composable
fun RideOptionItem(ride: RideOption, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(ride.icon, null, tint = contentColor)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(ride.name, color = contentColor, fontWeight = FontWeight.Bold)
            Text(ride.time, color = if(isSelected) contentColor.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Text(ride.price, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, isHighLighted: Boolean) {
    val bgColor = if (isHighLighted) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent

    BasicTextField(
        value = value, onValueChange = onValueChange,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().height(40.dp).background(bgColor, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                inner()
            }
        }
    )
}

@Composable
fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(result.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(result.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
fun InProgressMapOverlays(eta: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp, start = 20.dp, end = 20.dp)) {
        Surface(modifier = Modifier.align(Alignment.TopCenter), color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Arriving in", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(eta, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(modifier = Modifier.align(Alignment.TopEnd).size(48.dp).background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
fun MenuFloatingButton(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.size(48.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SimulatedMap(driverPosition: LatLng?) {
    PermissionsRequester(
        permissions = listOf(PermissionRequest(Permission.LOCATION, "Location", "User location", Icons.Default.LocationOn)),
        onAllPermissionsGranted = {}
    ) { controller ->
        val locationTracker = remember(controller) { getLocationTracker(controller) }
        BindLocationTrackerEffect(locationTracker = locationTracker)
        val tracker = remember(locationTracker) { GeoLocationTracker(locationTracker) }
        LaunchedEffect(Unit) { tracker.startTracking() }
        DisposableEffect(Unit) { onDispose { tracker.stopTracking() } }

        val coordinate by tracker.coordinate.collectAsState()
        val mappedDriver = driverPosition?.let { com.triapp.domain.model.Coordinate(it.lat, it.lng) }

        MapViewComponent(modifier = Modifier.fillMaxSize(), coordinate = coordinate, driverCoordinate = mappedDriver)
    }
}