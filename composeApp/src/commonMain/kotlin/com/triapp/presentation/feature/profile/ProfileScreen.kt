package com.triapp.presentation.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileFlowScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val uiState by viewModel.state.collectAsState()
    val onAction: (ProfileIntent) -> Unit = viewModel::processIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.LoggedOut -> onLogout()
                is ProfileEffect.ShowToast -> { /* toast */ }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (uiState.currentStep != ProfileStep.Main) {
                ProfileTopBar(
                    title = uiState.currentStep.title,
                    subtitle = "",
                    onBack = { onAction(ProfileIntent.Back) }
                )
            }
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                slideInHorizontally(tween(300)) { it } + fadeIn() togetherWith
                        slideOutHorizontally(tween(300)) { -it } + fadeOut()
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) { step ->

            when (step) {
                ProfileStep.Main -> ProfileMainScreen(
                    name = uiState.name,
                    email = uiState.email,
                    phone = uiState.phone,
                    onNavigate = { onAction(ProfileIntent.Navigate(it)) },
                    onLogout = { onAction(ProfileIntent.Logout) }
                )

                ProfileStep.Wallet -> WalletScreen()

                ProfileStep.History -> HistoryScreen()
            }
        }
    }
}

// ================== TELA 1: PERFIL PRINCIPAL ==================
@Composable
fun ProfileMainScreen(
    name: String,
    email: String,
    phone: String,
    onNavigate: (ProfileStep) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(name, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        InfoRow(icon = Icons.Outlined.Email, text = email)
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(icon = Icons.Outlined.Phone, text = phone)

        Spacer(modifier = Modifier.height(32.dp))

        MenuButton(
            icon = Icons.Outlined.AccountBalanceWallet,
            title = "Carteira",
            subtitle = "Gerenciar pagamentos",
            onClick = { onNavigate(ProfileStep.Wallet) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
            icon = Icons.Outlined.History,
            title = "Histórico",
            subtitle = "Ver corridas anteriores",
            onClick = { onNavigate(ProfileStep.History) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da conta")
        }
    }
}

// ================== TELA 2: CARTEIRA ==================
@Composable
fun WalletScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Card de Saldo (Branco)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Saldo disponível", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("R$ 45.00", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Adicionar saldo", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Sacar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Seção Métodos de Pagamento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Formas de pagamento", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", color = Color.Black, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Cartões
        PaymentMethodItem(
            icon = Icons.Default.CreditCard,
            color = Color(0xFF4B89FF),
            title = "Visa",
            subtitle = "•••• 4242",
            extraInfo = "Crédito",
            isDefault = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        PaymentMethodItem(
            icon = Icons.Default.CreditCard,
            color = Color(0xFFA855F7),
            title = "Mastercard",
            subtitle = "•••• 8888",
            extraInfo = "Débito",
            isSelected = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        PaymentMethodItem(
            icon = Icons.Default.QrCode,
            color = Color(0xFF2DD4BF),
            title = "PIX",
            subtitle = "PIX",
            extraInfo = null,
            isSelected = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        PaymentMethodItem(
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF22C55E),
            title = "Dinheiro",
            subtitle = "Dinheiro",
            extraInfo = null,
            isSelected = true
        )
    }
}

// ================== TELA 3: HISTÓRICO ==================
@Composable
fun HistoryScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Stats do Histórico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(Modifier.weight(1f), "5", "Corridas")
            StatCard(Modifier.weight(1f), "R$ 108.90", "Total gasto")
            StatCard(Modifier.weight(1f), "★ 4.8", "Média")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chips de Filtro
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            FilterChip(text = "Todas", selected = true)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(text = "Este mês", selected = false)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(text = "Esta semana", selected = false)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Corridas
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) { index ->
                RideHistoryItem(index)
            }
        }
    }
}

// ================== Componentes Reutilizáveis ==================

@Composable
fun ProfileTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MenuButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun PaymentMethodItem(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    extraInfo: String?,
    isDefault: Boolean = false,
    isSelected: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (subtitle.isNotEmpty()) {
                    Text(" $subtitle", color = MaterialTheme.colorScheme.primary)
                }
                if (isDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Padrão", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (extraInfo != null) {
                Text(extraInfo, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }

        // Ações (Check ou Lixeira)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RideHistoryItem(index: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if(index == 0) "06 Nov 2025 • 14:30" else "05 Nov 2025 • 09:15", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                Row { repeat(5) { Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp)) } }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.secondary))
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Origem", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(if(index == 0) "Rua Augusta, 2000" else "Av. Paulista, 1578", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Destino", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(if(index == 0) "Shopping Iguatemi" else "Aeroporto de Congonhas", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Motorista", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text("Carlos Silva", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4.2 km • 12 min", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(if(index == 0) "R$ 15.90" else "R$ 32.50", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Visa •••• 4242", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileFlowPreview() {
    ProfileFlowScreen({},{})
}