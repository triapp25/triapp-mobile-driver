package com.triapp.presentation.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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

// ================== Design System & Cores ==================
object ProfileColors {
    val Background = Color(0xFF000000)
    val SurfaceDark = Color(0xFF1C1C1E)
    val SurfaceLight = Color(0xFF2C2C2E)
    val White = Color(0xFFFFFFFF)
    val TextGray = Color(0xFF8E8E93)
    val Divider = Color(0xFF2C2C2E)
    val Red = Color(0xFFFF453A)
    val Green = Color(0xFF32D74B)
    val Gold = Color(0xFFFFD60A)
}

// Estados de Navegação Interna
enum class ProfileScreenState {
    MainProfile,
    Wallet,
    History
}

// ================== Orquestrador do Fluxo ==================
@Composable
fun ProfileFlow() {
    var currentScreen by remember { mutableStateOf(ProfileScreenState.MainProfile) }

    Scaffold(
        containerColor = ProfileColors.Background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Header de Navegação (Comum ou específico por tela)
            if (currentScreen != ProfileScreenState.MainProfile) {
                ProfileTopBar(
                    title = if (currentScreen == ProfileScreenState.Wallet) "Carteira" else "Histórico",
                    subtitle = if (currentScreen == ProfileScreenState.Wallet) "Gerencie seus métodos de pagamento" else "Suas corridas anteriores",
                    onBack = { currentScreen = ProfileScreenState.MainProfile }
                )
            } else {
                // Header da Home do Perfil (Botão voltar e Toggle Mode)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { /* Fechar Perfil */ },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(ProfileColors.SurfaceLight, RoundedCornerShape(8.dp))
                            .size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ProfileColors.White)
                    }
                }
            }

            // Transição de Telas
            AnimatedContent(
                targetState = currentScreen,
                label = "ProfileTransition",
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
            ) { screen ->
                when (screen) {
                    ProfileScreenState.MainProfile -> ProfileMainScreen(
                        onNavigate = { currentScreen = it }
                    )
                    ProfileScreenState.Wallet -> WalletScreen()
                    ProfileScreenState.History -> HistoryScreen()
                }
            }
        }
    }
}

// ================== TELA 1: PERFIL PRINCIPAL ==================
@Composable
fun ProfileMainScreen(onNavigate: (ProfileScreenState) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar e Nome
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(ProfileColors.SurfaceLight, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, null, tint = ProfileColors.White, modifier = Modifier.size(50.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("João Santos", color = ProfileColors.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { Icon(Icons.Default.StarBorder, null, tint = ProfileColors.TextGray, modifier = Modifier.size(16.dp)) }
            Spacer(modifier = Modifier.width(4.dp))
            Text("5.0", color = ProfileColors.TextGray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Editar perfil", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(Modifier.weight(1f), "142", "Corridas")
            StatCard(Modifier.weight(1f), "8 meses", "Membro")
            StatCard(Modifier.weight(1f), "R$ 2.4k", "Gasto")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Pessoal (Read Only)
        InfoRow(icon = Icons.Outlined.Email, text = "joao.santos@email.com")
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(icon = Icons.Outlined.Phone, text = "+55 11 98765-4321")

        Spacer(modifier = Modifier.height(32.dp))

        // Menu de Navegação
        MenuButton(
            icon = Icons.Outlined.AccountBalanceWallet,
            title = "Carteira",
            subtitle = "Gerenciar pagamentos",
            onClick = { onNavigate(ProfileScreenState.Wallet) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MenuButton(
            icon = Icons.Outlined.History,
            title = "Histórico",
            subtitle = "Ver corridas anteriores",
            onClick = { onNavigate(ProfileScreenState.History) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MenuButton(
            icon = Icons.Outlined.Place,
            title = "Endereços salvos",
            subtitle = "Casa, trabalho e mais",
            onClick = {}
        )
        Spacer(modifier = Modifier.height(12.dp))
        MenuButton(
            icon = Icons.Outlined.Settings,
            title = "Configurações",
            subtitle = "Preferências do app",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão Sair
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfileColors.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProfileColors.SurfaceLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da conta")
        }
        Spacer(modifier = Modifier.height(32.dp))
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
            colors = CardDefaults.cardColors(containerColor = ProfileColors.White)
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
            Text("Formas de pagamento", color = ProfileColors.TextGray, fontSize = 14.sp)
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = ProfileColors.White),
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
                .background(ProfileColors.SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ProfileColors.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = ProfileColors.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = ProfileColors.TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(ProfileColors.SurfaceDark, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = ProfileColors.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = ProfileColors.TextGray, fontSize = 12.sp)
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProfileColors.SurfaceLight, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = ProfileColors.TextGray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = ProfileColors.White)
    }
}

@Composable
fun MenuButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProfileColors.SurfaceDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ProfileColors.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ProfileColors.White, fontWeight = FontWeight.Bold)
            Text(subtitle, color = ProfileColors.TextGray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ProfileColors.TextGray)
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
            .border(1.dp, ProfileColors.SurfaceLight, RoundedCornerShape(16.dp))
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
                Text(title, color = ProfileColors.White, fontWeight = FontWeight.Bold)
                if (subtitle.isNotEmpty()) {
                    Text(" $subtitle", color = ProfileColors.White)
                }
                if (isDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ProfileColors.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Padrão", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (extraInfo != null) {
                Text(extraInfo, color = ProfileColors.TextGray, fontSize = 12.sp)
            }
        }

        // Ações (Check ou Lixeira)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ProfileColors.SurfaceLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = ProfileColors.TextGray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ProfileColors.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Delete, null, tint = ProfileColors.Red, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (selected) ProfileColors.White else ProfileColors.SurfaceLight,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.Black else ProfileColors.TextGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RideHistoryItem(index: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ProfileColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if(index == 0) "06 Nov 2025 • 14:30" else "05 Nov 2025 • 09:15", color = ProfileColors.TextGray, fontSize = 12.sp)
                Row { repeat(5) { Icon(Icons.Default.Star, null, tint = ProfileColors.White, modifier = Modifier.size(12.dp)) } }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = ProfileColors.TextGray, modifier = Modifier.size(16.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(ProfileColors.TextGray))
                    Icon(Icons.Outlined.LocationOn, null, tint = ProfileColors.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Origem", color = ProfileColors.TextGray, fontSize = 10.sp)
                    Text(if(index == 0) "Rua Augusta, 2000" else "Av. Paulista, 1578", color = ProfileColors.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Destino", color = ProfileColors.TextGray, fontSize = 10.sp)
                    Text(if(index == 0) "Shopping Iguatemi" else "Aeroporto de Congonhas", color = ProfileColors.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = ProfileColors.Divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Motorista", color = ProfileColors.TextGray, fontSize = 10.sp)
                    Text("Carlos Silva", color = ProfileColors.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4.2 km • 12 min", color = ProfileColors.TextGray, fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor", color = ProfileColors.TextGray, fontSize = 10.sp)
                    Text(if(index == 0) "R$ 15.90" else "R$ 32.50", color = ProfileColors.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Visa •••• 4242", color = ProfileColors.TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileFlowPreview() {
    ProfileFlow()
}