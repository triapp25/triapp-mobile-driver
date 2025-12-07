package com.triappdriver.presentation.feature.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triappdriver.BackHandler
import com.triappdriver.domain.model.RideHistoryDomainModel
import com.triappdriver.domain.model.WalletDomainModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileFlowScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val uiState by viewModel.state.collectAsState()
    val onAction: (ProfileIntent) -> Unit = viewModel::processIntent

    // --- ESTADOS LOCAIS PARA CONTROLAR OS BOTTOM SHEETS ---
    var showAddCardSheet by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<WalletDomainModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler {
        when(uiState.currentStep) {
            ProfileStep.Main -> onBack()
            ProfileStep.Wallet -> onAction(ProfileIntent.Back)
            ProfileStep.History -> onAction(ProfileIntent.Back)
        }
    }

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

                ProfileStep.Wallet -> BankAccountCard(
                    //balance = uiState.walletBalance,
                ){}

                ProfileStep.History -> HistoryScreen(historyList = uiState.rideHistory)
            }
        }

        // --- BOTTOM SHEET DE ADICIONAR CARTÃO ---
        if (showAddCardSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddCardSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AddCardBottomSheetContent(
                    onCancel = { showAddCardSheet = false },
                    onSave = { cardNumber, cardName ->
                        // Aqui você enviaria o Intent para o ViewModel
                        // onAction(ProfileIntent.AddNewCard(...))
                        showAddCardSheet = false
                    }
                )
            }
        }

        // --- BOTTOM SHEET DE CONFIRMAÇÃO DE DELETE ---
        if (cardToDelete != null) {
            ModalBottomSheet(
                onDismissRequest = { cardToDelete = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DeleteConfirmationSheetContent(
                    card = cardToDelete!!,
                    onCancel = { cardToDelete = null },
                    onConfirm = {
                        onAction(ProfileIntent.DeleteCard(cardToDelete!!))
                        cardToDelete = null
                    }
                )
            }
        }
    }
}

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
            Icon(
                Icons.Outlined.Person,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            name,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

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

// ================== CONTEÚDO DOS SHEETS ==================

@Composable
fun AddCardBottomSheetContent(
    onCancel: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp) // Espaço extra para a barra de navegação
    ) {
        Text("Adicionar novo cartão", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Número do cartão") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cardName,
            onValueChange = { cardName = it },
            label = { Text("Nome do titular") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Validade (MM/AA)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSave(cardNumber, cardName) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Salvar Cartão", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeleteConfirmationSheetContent(
    card: WalletDomainModel,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Remover cartão?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tem certeza que deseja remover o cartão ${card.title} terminado em ${card.subtitle}?",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remover", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================== TELA 2: CARTEIRA (ATUALIZADA) ==================

@Composable
fun BankAccountCard(
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FoodBank,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("Banco do Brasil", color = Color.White, fontSize = 20.sp)
                    Text("Conta Corrente", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Principal", color = Color.Black, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            InfoRow("Agência", "1234-5")
            InfoRow("Conta", "12345678-9")
            InfoRow("Titular", "Carlos Silva")

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun InfoRow(left: String, right: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(left, color = Color.Gray, fontSize = 14.sp)
        Text(right, color = Color.White, fontSize = 14.sp)
    }
}

// ================== TELA 3: HISTÓRICO ==================
@Composable
fun HistoryScreen(
    historyList: List<RideHistoryDomainModel>
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Chips de Filtro
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            FilterChip(text = "Todas", selected = true)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(text = "Este mês", selected = false)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(text = "Esta semana", selected = false)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(historyList) { historyItem ->
                RideHistoryItem(historyItem)
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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
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
        Text(
            value,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
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
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = MaterialTheme.colorScheme.secondary
        )
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
    isSelected: Boolean = false,
    onDelete: () -> Unit
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
                        Text(
                            "Padrão",
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (extraInfo != null) {
                Text(extraInfo, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clickable {
                    onDelete()
                }
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Delete,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
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
fun RideHistoryItem(item: RideHistoryDomainModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.date, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                Row {
                    repeat(5) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.RadioButtonUnchecked,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(
                        modifier = Modifier.width(1.dp).height(24.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Origem", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(item.origin, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Destino", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(
                        item.destination,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            ) // Use HorizontalDivider no Material3
            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Passageiro", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(
                        item.riderName,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.distanceTime,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                    Text(
                        item.price,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.paymentMethod,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}