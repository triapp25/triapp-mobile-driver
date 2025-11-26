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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triapp.TriAppTheme
import com.triapp.TriColors
import com.triapp.domain.model.SignUpDomainModel
import com.triapp.presentation.feature.login.AppButton
import com.triapp.presentation.feature.login.AppLogo
import com.triapp.presentation.feature.login.AppTextField
import com.triapp.presentation.feature.login.OtpInputField
import com.triapp.presentation.feature.login.PhoneNumberVisualTransformation
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignupFlowScreen(activity: Any?, onNavigate: () -> Unit) {
    val viewModel = koinViewModel<SignupViewModel>()
    val uiState by viewModel.state.collectAsState()
    val onAction: (SignupIntent) -> Unit = viewModel::processIntent

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (uiState.currentStep != SignupStep.Success) {
                SignupTopBar(
                    step = uiState.currentStep,
                    onBack = { onAction(SignupIntent.PreviousStep) }
                )
            }
        },
        bottomBar = {
            if (uiState.currentStep != SignupStep.Verification && uiState.currentStep != SignupStep.Success) {
                Button(
                    onClick = { onAction(SignupIntent.NextStep) },
                    enabled = uiState.canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .alpha(if (uiState.canContinue) 1f else 0.4f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally(tween(300)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(tween(300)) { -it } + fadeOut()
                } else {
                    slideInHorizontally(tween(300)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(tween(300)) { it } + fadeOut()
                }
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                when (step) {
                    SignupStep.PersonalInfo -> StepPersonalInfo(uiState, onAction)
                    SignupStep.Contact -> StepContact(activity, uiState, onAction)
                    SignupStep.Security -> StepSecurity(uiState, onAction)
                    SignupStep.Documents -> StepDocuments(
                        onUploadProfile = { path -> onAction(SignupIntent.UploadProfilePhoto(path)) },
                        onUploadId = { path -> onAction(SignupIntent.UploadIdDocument(path)) }
                    )

                    SignupStep.Verification -> SmsVerificationScreen(
                        phoneNumber = uiState.phone,
                        onVerifyClick = { code -> onAction(SignupIntent.VerifyCode(code)) },
                        onResendClick = { onAction(SignupIntent.SendResetCode(activity = activity)) }
                    )

                    SignupStep.Success -> StepSuccess(uiState, onNavigate)
                }
            }
        }
    }
}

/* ================== Top Bar ================== */
@Composable
fun SignupTopBar(step: SignupStep, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = step.progress,
                    modifier = Modifier
                        .width(150.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${step.ordinal + 1}/6",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/* ================== Steps ================== */

@Composable
fun StepPersonalInfo(uiState: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column {
        Text("Personal Information", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text(
            "Tell us about yourself",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Full name",
            value = uiState.fullName,
            onValueChange = { onAction(SignupIntent.EnterFullName(it)) },
            placeholder = "John Doe"
        )
    }
}

@Composable
fun StepContact(activity: Any?, uiState: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    Column {
        Text("Contact details", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text(
            "How can we reach you?",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Email address",
            value = uiState.email,
            onValueChange = { onAction(SignupIntent.EnterEmail(it)) },
            placeholder = "john@example.com",
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it.filter(Char::isDigit).take(11)
                if (phoneNumber.length == 11) onAction(
                    SignupIntent.EnterPhone(
                        activity,
                        phoneNumber
                    )
                )
            },
            visualTransformation = PhoneNumberVisualTransformation(),
            placeholder = "(11) 99999-9999",
            icon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone
        )
    }
}

@Composable
fun StepSecurity(uiState: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column {
        Text("Secure your account", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text(
            "Create a strong password",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            label = "Password",
            value = uiState.password,
            onValueChange = { onAction(SignupIntent.EnterPassword(it)) },
            placeholder = "********",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            label = "Confirm password",
            value = uiState.confirmPassword,
            onValueChange = { onAction(SignupIntent.EnterConfirmPassword(it)) },
            placeholder = "********",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )
    }
}

@Composable
fun StepDocuments(
    onUploadProfile: (String) -> Unit,
    onUploadId: (String) -> Unit
) {
    var selectedFileProfileFirst by remember { mutableStateOf<PlatformFile?>(null) }
    var selectedFileProfileSecond by remember { mutableStateOf<PlatformFile?>(null) }
    var selectedFileIdFirst by remember { mutableStateOf<PlatformFile?>(null) }
    var selectedFileIdSecond by remember { mutableStateOf<PlatformFile?>(null) }

    var uploadStatusFirst by remember { mutableStateOf("") }
    var uploadStatusSecond by remember { mutableStateOf("") }

    val pickerLauncherFirst = rememberFilePickerLauncher(
        mode = FileKitMode.Single
    ) { file ->
        selectedFileProfileFirst = file
        selectedFileProfileFirst?.let { onUploadProfile(it.name) }
        uploadStatusFirst =
            if (selectedFileProfileFirst != null) "Arquivo selecionado: ${selectedFileProfileFirst?.name}" else "Nenhum arquivo selecionado"
    }

    val pickerLauncherSecond = rememberFilePickerLauncher(
        mode = FileKitMode.Single
    ) { file ->
        selectedFileIdSecond = file
        selectedFileIdSecond?.let { onUploadId(it.name) }
        uploadStatusSecond =
            if (selectedFileIdSecond != null) "Arquivo selecionado: ${selectedFileIdSecond?.name}" else "Nenhum arquivo selecionado"
    }

    Column {
        Text("Document verification", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text(
            "Verify your identity",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Profile photo", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text("A clear photo of your face", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        DocumentUploadCard(title = "Drop your file here or browse") {
            pickerLauncherFirst.launch()
        }

        if (uploadStatusFirst.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(uploadStatusFirst, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }


        Spacer(modifier = Modifier.height(24.dp))

        Text("ID Document", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text("Government-issued ID or passport", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        DocumentUploadCard(title = "Drop your file here or browse") {
            pickerLauncherSecond.launch()
        }

        if (uploadStatusSecond.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(uploadStatusSecond, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun SmsVerificationScreen(
    phoneNumber: String,
    onVerifyClick: (String) -> Unit,
    onResendClick: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(21) }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AppLogo(
            title = "Verificação SMS",
            subtitle = "Enviamos um código de 6 dígitos para\n$phoneNumber",
            icon = Icons.Outlined.Smartphone,
            subtitleColor = MaterialTheme.colorScheme.secondary,
            isSubtitleBold = false
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Digite o código",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Insira o código recebido por SMS",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OtpInputField(code = code, onCodeChange = { if (it.length <= 6) code = it })

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Verificar código",
                    onClick = { onVerifyClick(code) },
                    enabled = code.length == 6
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Não recebeu o código?", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = timeLeft == 0) {
                        if (timeLeft == 0) {
                            timeLeft = 21
                            onResendClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (timeLeft == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (timeLeft > 0) "Reenviar em ${timeLeft}s" else "Reenviar agora",
                        color = if (timeLeft == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Text(
            text = "O código expira em 10 minutos",
            color = TriColors.DarkText,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun StepSuccess(data: SignUpDomainModel, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "All set!",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Your account has been created successfully",
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Get started", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Black)
        }
    }
}

/* ================== Helpers UI ================== */

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
        Text(
            label,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = if (icon != null) {
                { Icon(icon, null, tint = MaterialTheme.colorScheme.secondary) }
            } else null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}

@Composable
fun DocumentUploadCard(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CloudUpload,
                null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            Text("PDF, PNG, or JPG (max. 10MB)", color = Color.DarkGray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery", fontSize = 12.sp)
                }
                //Spacer(modifier = Modifier.width(12.dp))
                //Button(
                //    onClick = onClick,
                //    colors = ButtonDefaults.buttonColors(containerColor = SignupColors.SurfaceDark),
                //    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                //    modifier = Modifier.height(32.dp)
                //) {
                //    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                //    Spacer(modifier = Modifier.width(4.dp))
                //    Text("Camera", fontSize = 12.sp)
                //}
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
        Text(label, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        Text(
            value,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}