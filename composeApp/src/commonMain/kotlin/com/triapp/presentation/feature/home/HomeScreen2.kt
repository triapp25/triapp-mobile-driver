package com.triapp.presentation.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triapp.presentation.feature.profile.ProfileFlowScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ================== Cores do Tema ==================
val AppBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF1C1C1E)
val PrimaryWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF8E8E93)
val AccentBlue = Color(0xFF4B89FF) // Para marcadores de mapa
val AccentGreen = Color(0xFF34C759) // Para sucesso

// ================== Estados da Viagem ==================
enum class HomeRideState {
    Initial,        // Tela inicial (Onde ir?)
    RideSelection,  // Escolhendo carro (Economy, Comfort)
    Searching,      // Radar procurando
    DriverFound,     // Motorista a caminho
}

// Modelo de dados simples para os carros
data class RideOption(val name: String, val price: String, val time: String, val icon: ImageVector)

@Composable
fun HomeScreenTwo(onNavigate: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado atual da tela
    var currentStep by remember { mutableStateOf(HomeRideState.Initial) }

    // Simulação de fluxo automático (Do searching para Found)
    LaunchedEffect(currentStep) {
        if (currentStep == HomeRideState.Searching) {
            delay(3000) // Simula 3 segundos procurando
            currentStep = HomeRideState.DriverFound
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileFlowScreen({},{})
        }
    ) {
        Scaffold(
            containerColor = AppBlack,
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. Mapa de Fundo (Simulado)
                SimulatedMap(currentStep)

                // 2. Botão de Menu Flutuante (Canto Superior Esquerdo)
                MenuFloatingButton(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 20.dp)
                        .align(Alignment.TopStart),
                    onClick = { scope.launch { drawerState.open() } }
                )

                // 3. Conteúdo Inferior (Bottom Sheet Dinâmico)
                RideBottomSheet(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    step = currentStep,
                    onStateChange = { newState -> currentStep = newState },
                    onNavigate
                )
            }
        }
    }
}

// ================== Componentes de UI ==================

@Composable
fun RideBottomSheet(
    modifier: Modifier = Modifier,
    step: HomeRideState,
    onStateChange: (HomeRideState) -> Unit,
    onNavigate: () -> Unit
) {
    // Card principal com cantos arredondados no topo
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = SurfaceDark
    ) {
        // Animação de transição de conteúdo
        AnimatedContent(
            targetState = step,
            label = "RideStateTransition",
            modifier = Modifier.padding(24.dp)
        ) { targetStep ->
            when (targetStep) {
                HomeRideState.Initial -> InitialStateView(onDestinationClick = { onStateChange(HomeRideState.RideSelection) })
                HomeRideState.RideSelection -> RideSelectionView(onConfirm = { onStateChange(HomeRideState.Searching) })
                HomeRideState.Searching -> SearchingView(onCancel = { onStateChange(HomeRideState.Initial) })
                HomeRideState.DriverFound -> DriverFoundView(onCancel = {
                    //onStateChange(HomeRideState.Initial)
                    onNavigate()
                })
            }
        }
    }
}

// --- Views dos Estados ---

@Composable
fun InitialStateView(onDestinationClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.DarkGray, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Where to?", color = PrimaryWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de busca fake
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF2C2C2E), RoundedCornerShape(12.dp))
                .clickable { onDestinationClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = PrimaryWhite)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Enter destination", color = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Atalhos Recentes
        RecentItem(icon = Icons.Default.Schedule, title = "Av. Paulista, 1578", subtitle = "Bela Vista, São Paulo")
        Spacer(modifier = Modifier.height(16.dp))
        RecentItem(icon = Icons.Default.Star, title = "Shopping Iguatemi", subtitle = "Jardim Paulistano")

        Spacer(modifier = Modifier.height(24.dp))

        // Botões Home/Work
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShortcutCard(Modifier.weight(1f), "Home", Icons.Default.Home)
            ShortcutCard(Modifier.weight(1f), "Work", Icons.Default.Work)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RideSelectionView(onConfirm: () -> Unit) {
    val rides = listOf(
        RideOption("Economy", "R$ 12.50", "2 min", Icons.Default.FlashOn),
        RideOption("Standard", "R$ 15.90", "1 min", Icons.Default.DirectionsCar),
        RideOption("Comfort", "R$ 22.40", "3 min", Icons.Default.Star)
    )
    var selectedRide by remember { mutableStateOf(0) }

    Column {
        Text("Choose your ride", color = TextGray, fontSize = 14.sp)

        // Resumo da rota
        Row(Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Pickup", color = TextGray, fontSize = 12.sp)
                Text("Rua Augusta, 2000", color = PrimaryWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dropoff", color = TextGray, fontSize = 12.sp)
                Text("Av. Paulista, 1578", color = PrimaryWhite, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = Color.DarkGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Carros
        rides.forEachIndexed { index, ride ->
            val isSelected = selectedRide == index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (isSelected) PrimaryWhite else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if(isSelected) Color.Transparent else Color.DarkGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedRide = index }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(ride.icon, null, tint = if(isSelected) AppBlack else PrimaryWhite)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ride.name, color = if(isSelected) AppBlack else PrimaryWhite, fontWeight = FontWeight.Bold)
                    Text("Affordable rides", color = if(isSelected) Color.DarkGray else TextGray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(ride.price, color = if(isSelected) AppBlack else PrimaryWhite, fontWeight = FontWeight.Bold)
                    Text(ride.time, color = if(isSelected) Color.DarkGray else TextGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pagamento
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, null, tint = PrimaryWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Card **** 4242", color = PrimaryWhite)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, null, tint = TextGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirm ride", color = AppBlack, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SearchingView(onCancel: () -> Unit) {
    // Animação de radar
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = ""
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            Box(modifier = Modifier.size(100.dp).background(Color(0xFF2C2C2E).copy(alpha = 0.5f), CircleShape))
            Box(modifier = Modifier.size((80 * scale).dp).background(Color(0xFF2C2C2E), CircleShape))
            Text("Procurando motorista", color = TextGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Aguarde enquanto encontramos o melhor", color = TextGray)
        Text("motorista para você", color = TextGray)

        Spacer(modifier = Modifier.height(32.dp))

        // Métricas falsas
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricItem("127", "Motoristas")
            MetricItem("2.3 km", "Distância")
            MetricItem("3 min", "Tempo")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
        ) {
            Text("Cancelar busca")
        }
    }
}

@Composable
fun DriverFoundView(onCancel: () -> Unit) {
    Column {
        // Cabeçalho de Sucesso
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(AccentGreen, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = AppBlack)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Motorista encontrado!", color = PrimaryWhite, fontWeight = FontWeight.Bold)
                Text("Chegando em 2 minutos", color = TextGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card do Motorista
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = TextGray, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Carlos Silva", color = PrimaryWhite, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = PrimaryWhite, modifier = Modifier.size(12.dp))
                        Text(" 5.0", color = PrimaryWhite, fontSize = 12.sp)
                    }
                }
                // Botões de ação
                IconButton(onClick = {}, modifier = Modifier.background(Color(0xFF2C2C2E), CircleShape)) {
                    Icon(Icons.Default.Phone, null, tint = PrimaryWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {}, modifier = Modifier.background(Color(0xFF2C2C2E), CircleShape)) {
                    Icon(Icons.Default.Message, null, tint = PrimaryWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info do Carro
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Veículo", color = TextGray, fontSize = 12.sp)
                Text("Honda Civic", color = PrimaryWhite)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Placa", color = TextGray, fontSize = 12.sp)
                Text("ABC-1234", color = PrimaryWhite)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
        ) {
            Text("Cancelar busca")
        }
    }
}

// ================== Componentes Auxiliares ==================

@Composable
fun SimulatedMap(step: HomeRideState) {
    // Um Canvas que desenha coisas diferentes baseado no estado
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        val width = size.width
        val height = size.height

        // Desenha ruas (fictício)
        drawLine(Color(0xFF1F1F1F), start = Offset(0f, height*0.3f), end = Offset(width, height*0.4f), strokeWidth = 30f)
        drawLine(Color(0xFF1F1F1F), start = Offset(width*0.2f, 0f), end = Offset(width*0.4f, height), strokeWidth = 30f)

        // Posição do usuário (Centro)
        val userPos = Offset(width / 2, height * 0.4f)

        // Posição do destino (Simulada)
        val destPos = Offset(width * 0.8f, height * 0.2f)

        // Desenha rota se tiver selecionado destino
        if (step != HomeRideState.Initial) {
            drawLine(
                color = PrimaryWhite,
                start = userPos,
                end = destPos,
                strokeWidth = 8f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
            drawCircle(AccentBlue, radius = 20f, center = destPos)
        }

        // Desenha usuário
        drawCircle(PrimaryWhite.copy(alpha = 0.2f), radius = 60f, center = userPos)
        drawCircle(PrimaryWhite, radius = 20f, center = userPos)
    }
}

@Composable
fun MenuFloatingButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(SurfaceDark, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryWhite)
    }
}

@Composable
fun ProfileDrawerContent() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(SurfaceDark)
            .padding(24.dp)
    ) {
        // Cabeçalho do Perfil
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.Gray, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("John Doe", color = PrimaryWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("5.0 ★", color = TextGray)
            }
        }
        Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.DarkGray)

        // Itens do Menu
        DrawerItem(Icons.Default.Payment, "Payment")
        DrawerItem(Icons.Default.History, "Ride History")
        DrawerItem(Icons.Default.Settings, "Settings")
        DrawerItem(Icons.Default.Support, "Support")
        Spacer(modifier = Modifier.weight(1f))
        Text("Log out", color = TextGray)
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PrimaryWhite)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = PrimaryWhite, fontSize = 16.sp)
    }
}

@Composable
fun RecentItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFF2C2C2E), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TextGray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = PrimaryWhite, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
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
        Icon(icon, null, tint = PrimaryWhite)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = PrimaryWhite, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MetricItem(value: String, label: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = PrimaryWhite, fontWeight = FontWeight.Bold)
        Text(label, color = TextGray, fontSize = 10.sp)
    }
}