package com.triapp.presentation.feature.signup

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel

// ================== Modelos e Estados ==================

enum class SignupStep(val title: String, val progress: Float) {
    PersonalInfo("Personal Information", 0.16f),
    Contact("Contact details", 0.33f),
    Security("Secure your account", 0.5f),
    Documents("Document verification", 0.66f),
    Verification("Verificação SMS", 0.83f),
    Success("All set!", 1.0f)
}

data class SignupData(
    var fullName: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var documentPath: String? = null
)

// Cores (Consistentes com o design anterior)
object SignupColors {
    val Background = Color(0xFF000000)
    val SurfaceDark = Color(0xFF1C1C1E)
    val PrimaryWhite = Color(0xFFFFFFFF)
    val TextGray = Color(0xFF8E8E93)
    val InputBg = Color(0xFF2C2C2E)
}

@Composable
fun SignupFlowScreen(onNavigate: () -> Unit) {

    val viewModel = koinViewModel<SignupViewModel>()
    val uiState by viewModel.state.collectAsState()

    val onAction: (SignupIntent) -> Unit = viewModel::processIntent

    var currentStep by remember { mutableStateOf(SignupStep.PersonalInfo) }
    // Estado compartilhado para armazenar os dados preenchidos
    val signupData = remember { mutableStateOf(SignupData()) }

    Scaffold(
        containerColor = SignupColors.Background,
        topBar = {
            if (currentStep != SignupStep.Success) {
                SignupTopBar(
                    step = currentStep,
                    onBack = {
                        val previousStep = SignupStep.values().getOrNull(currentStep.ordinal - 1)
                        if (previousStep != null) {
                            currentStep = previousStep
                        } else {
                            // Cancelar fluxo ou fechar tela
                        }
                    }
                )
            }
        },
        bottomBar = {
            // O botão fica na parte inferior para os passos 1 a 4.
            // O passo 5 (SMS) e 6 (Sucesso) têm layouts levemente diferentes,
            // mas podemos padronizar ou customizar dentro do conteúdo.
            if (currentStep != SignupStep.Verification && currentStep != SignupStep.Success) {
                Button(
                    onClick = {
                        val nextStep = SignupStep.values().getOrNull(currentStep.ordinal + 1)
                        if (nextStep != null) currentStep = nextStep
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SignupColors.PrimaryWhite)
                ) {
                    Text(
                        text = "Continue",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
                }
            }
        }
    ) { paddingValues ->

        // Animação de transição entre telas
        AnimatedContent(
            targetState = currentStep,
            label = "SignupTransition",
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            }
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                when (step) {
                    SignupStep.PersonalInfo -> StepPersonalInfo(signupData)
                    SignupStep.Contact -> StepContact(signupData)
                    SignupStep.Security -> StepSecurity(signupData)
                    SignupStep.Documents -> StepDocuments()
                    SignupStep.Verification -> StepVerification(
                        phone = signupData.value.phone,
                        onVerified = { currentStep = SignupStep.Success }
                    )
                    SignupStep.Success -> StepSuccess(signupData.value, onNavigate)
                }
            }
        }
    }
}

// ================== Componentes da Top Bar ==================

@Composable
fun SignupTopBar(step: SignupStep, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Botão Voltar
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SignupColors.PrimaryWhite)
            }

            // Barra de Progresso
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { step.progress },
                    modifier = Modifier
                        .width(150.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SignupColors.PrimaryWhite,
                    trackColor = SignupColors.SurfaceDark,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${step.ordinal + 1}/6", // Ajuste manual pois Success conta como passo
                    color = SignupColors.TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ================== Passos Individuais ==================

// PASSO 1
@Composable
fun StepPersonalInfo(data: MutableState<SignupData>) {
    Column {
        Text("Personal Information", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("Tell us about yourself", color = SignupColors.PrimaryWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Full name",
            value = data.value.fullName,
            onValueChange = { data.value = data.value.copy(fullName = it) },
            placeholder = "John Doe"
        )
    }
}

// PASSO 2
@Composable
fun StepContact(data: MutableState<SignupData>) {
    Column {
        Text("Contact details", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("How can we reach you?", color = SignupColors.PrimaryWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Email address",
            value = data.value.email,
            onValueChange = { data.value = data.value.copy(email = it) },
            placeholder = "john@example.com",
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            label = "Phone number",
            value = data.value.phone,
            onValueChange = { data.value = data.value.copy(phone = it) },
            placeholder = "(11) 99999-9999",
            icon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone
        )
    }
}

// PASSO 3
@Composable
fun StepSecurity(data: MutableState<SignupData>) {
    Column {
        Text("Secure your account", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("Create a strong password", color = SignupColors.PrimaryWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Password",
            value = data.value.password,
            onValueChange = { data.value = data.value.copy(password = it) },
            placeholder = "********",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            label = "Confirm password",
            value = data.value.confirmPassword,
            onValueChange = { data.value = data.value.copy(confirmPassword = it) },
            placeholder = "********",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )
    }
}

// PASSO 4
@Composable
fun StepDocuments() {
    Column {
        Text("Document verification", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("Verify your identity", color = SignupColors.PrimaryWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Text("Profile photo", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("A clear photo of your face", color = SignupColors.TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        DocumentUploadCard(title = "Drop your file here or browse")

        Spacer(modifier = Modifier.height(24.dp))

        Text("ID Document", color = SignupColors.TextGray, fontSize = 14.sp)
        Text("Government-issued ID or passport", color = SignupColors.TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        DocumentUploadCard(title = "Drop your file here or browse")
    }
}

// PASSO 5 (Reutilizando lógica da tela SMS anterior)
@Composable
fun StepVerification(phone: String, onVerified: () -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(SignupColors.PrimaryWhite, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Smartphone, null, tint = Color.Black)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Verificação SMS", color = SignupColors.PrimaryWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enviamos um código de 6 dígitos para", color = SignupColors.TextGray, fontSize = 14.sp)
        Text(phone.ifEmpty { "(11) 99999-9999" }, color = SignupColors.PrimaryWhite, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        // Simulação do componente OTP
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            repeat(6) {
                Box(modifier = Modifier
                    .size(45.dp, 55.dp)
                    .background(SignupColors.InputBg, RoundedCornerShape(8.dp)))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Não recebeu o código?", color = SignupColors.TextGray)
        Text("Reenviar código", color = SignupColors.PrimaryWhite, fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onVerified,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B2A)), // Azul bem escuro ou preto com borda
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)) // Borda azul sutil
        ) {
            Text("Verify: Use 123456", color = Color.White) // Texto simulado
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// PASSO 6 - Sucesso
@Composable
fun StepSuccess(data: SignupData, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SignupColors.PrimaryWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("All set!", color = SignupColors.PrimaryWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Your account has been created successfully", color = SignupColors.TextGray, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))

        // Card de Resumo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignupColors.SurfaceDark, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            SuccessRow(label = "Name", value = data.fullName.ifEmpty { "N/A" })
            Spacer(modifier = Modifier.height(16.dp))
            SuccessRow(label = "Email", value = data.email.ifEmpty { "N/A" })
            Spacer(modifier = Modifier.height(16.dp))
            SuccessRow(label = "Role", value = "Passenger")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SignupColors.PrimaryWhite)
        ) {
            Text("Get started", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Black)
        }
    }
}

// ================== Helpers UI ==================

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, color = SignupColors.TextGray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = if (icon != null) { { Icon(icon, null, tint = SignupColors.TextGray) } } else null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SignupColors.InputBg,
                unfocusedContainerColor = SignupColors.InputBg,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = SignupColors.PrimaryWhite,
                unfocusedTextColor = SignupColors.PrimaryWhite
            ),
            singleLine = true
        )
    }
}

@Composable
fun DocumentUploadCard(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(SignupColors.InputBg, RoundedCornerShape(12.dp))
            // Simulação de borda tracejada (dashed border é complexo no compose nativo simples, usando sólida por hora)
            .border(1.dp, SignupColors.SurfaceDark, RoundedCornerShape(12.dp))
            .clickable { /* Abrir seletor de arquivo */ },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.CloudUpload, null, tint = SignupColors.TextGray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = SignupColors.TextGray, fontSize = 12.sp)
            Text("PDF, PNG, or JPG (max. 10MB)", color = Color.DarkGray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SignupColors.SurfaceDark), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SignupColors.SurfaceDark), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SuccessRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SignupColors.TextGray, fontSize = 14.sp)
        Text(value, color = SignupColors.PrimaryWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}