package com.triapp.presentation.feature.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ================== Design System (Cores) ==================
object AppColors {
    val Background = Color(0xFF000000)
    val CardBackground = Color(0xFF1C1C1E)
    val InputBackground = Color(0xFF2C2C2E)
    val White = Color(0xFFFFFFFF)
    val Gray = Color(0xFF8E8E93)
    val ButtonGray = Color(0xFF3A3A3C)
    val DarkText = Color(0xFF48484A)
}

// ================== Tela Principal ==================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: () -> Unit
) {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // SheetState atualizado para controlar melhor o comportamento
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showForgotPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is LoginEffect.ShowError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is LoginEffect.ShowForgotPassword -> showForgotPassword = true
                is LoginEffect.HideForgotPassword -> showForgotPassword = false
                is LoginEffect.NavigateToSignup -> onNavigate()
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        // Garante que o conteúdo não fique atrás da barra de navegação do sistema
        modifier = Modifier.navigationBarsPadding()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                // Adiciona padding quando o teclado abre
                .imePadding()
        ) {
            // Animação suave entre Login e SMS
            AnimatedContent(
                targetState = uiState.step,
                label = "LoginSteps",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) { step ->
                when (step) {
                    LoginStep.Login -> RideLoginScreen(
                        onLoginClick = { phone -> viewModel.processIntent(LoginIntent.SubmitPhone(phone)) }, // Exemplo de Intent com dados
                        onForgotPasswordClick = { viewModel.processIntent(LoginIntent.OpenForgotPassword) },
                        onSignUpClick = { viewModel.processIntent(LoginIntent.NavigateToSignup) }
                    )
                    LoginStep.ResetCode -> SmsVerificationScreen(
                        phoneNumber = "51 - 99823-2323", // Pegar do uiState na realidade
                        onVerifyClick = { code -> viewModel.processIntent(LoginIntent.VerifyCode(code)) },
                        onResendClick = { viewModel.processIntent(LoginIntent.SendResetCode) }
                    )
                }
            }

            if (showForgotPassword) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.processIntent(LoginIntent.DismissForgotPassword)
                        showForgotPassword = false
                    },
                    sheetState = sheetState,
                    containerColor = AppColors.CardBackground,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.Gray) }
                ) {
                    ForgotPasswordScreen(
                        onSendReset = { email ->
                            viewModel.processIntent(LoginIntent.RequestPasswordReset(email))
                            scope.launch { sheetState.hide() }.invokeOnCompletion { showForgotPassword = false }
                        },
                        onBackToLogin = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { showForgotPassword = false }
                        }
                    )
                }
            }
        }
    }
}

// ================== Telas Individuais ==================

@Composable
fun RideLoginScreen(
    onLoginClick: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo Section
        AppLogo(
            title = "Ride",
            subtitle = "Urban mobility reimagined",
            icon = Icons.Outlined.LocationOn
        )

        // Card Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.CardBackground,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome back",
                    color = AppColors.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in with your phone number",
                    color = AppColors.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "(11) 99999-9999",
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botão de ação principal (opcional na tela de login se for automático, mas bom ter)
                AppButton(text = "Continue", onClick = { onLoginClick(phoneNumber) })

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot password?",
                    color = AppColors.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val signUpText = buildAnnotatedString {
                    append("Don't have an account? ")
                    pushStringAnnotation(tag = "signup", annotation = "signup")
                    withStyle(style = SpanStyle(color = AppColors.White, textDecoration = TextDecoration.Underline)) {
                        append("Sign up")
                    }
                    pop()
                }

                ClickableText(
                    text = signUpText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.Gray),
                    onClick = { offset ->
                        signUpText.getStringAnnotations(tag = "signup", start = offset, end = offset)
                            .firstOrNull()?.let { onSignUpClick() }
                    }
                )
            }
        }

        // Footer
        val termsText = buildAnnotatedString {
            append("By continuing, you agree to our ")
            pushStringAnnotation(tag = "terms", annotation = "terms")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("Terms of Service")
            }
            pop()
        }

        ClickableText(
            text = termsText,
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = AppColors.Gray, textAlign = TextAlign.Center),
            onClick = { /* Handle Terms Click */ }
        )
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
            subtitleColor = AppColors.Gray,
            isSubtitleBold = false
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.CardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Digite o código",
                    color = AppColors.White,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Insira o código recebido por SMS",
                    color = AppColors.Gray,
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

                Text(text = "Não recebeu o código?", color = AppColors.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = timeLeft == 0) {
                        if(timeLeft == 0) {
                            timeLeft = 21
                            onResendClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if(timeLeft == 0) AppColors.White else AppColors.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (timeLeft > 0) "Reenviar em ${timeLeft}s" else "Reenviar agora",
                        color = if(timeLeft == 0) AppColors.White else AppColors.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Text(
            text = "O código expira em 10 minutos",
            color = AppColors.DarkText,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ForgotPasswordScreen(
    onSendReset: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    // Usamos Column direto pois está dentro de um ModalBottomSheet
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(AppColors.White, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Lock",
                tint = AppColors.Background,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Forgot password?",
            color = AppColors.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No worries, we'll send you reset instructions",
            color = AppColors.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter your email",
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(text = "Send reset code", onClick = { onSendReset(email) })

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Back to login",
            color = AppColors.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onBackToLogin() }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ================== Componentes Reutilizáveis (UI Kit) ==================

@Composable
fun AppLogo(
    title: String,
    subtitle: String,
    icon: ImageVector,
    subtitleColor: Color = AppColors.Gray,
    isSubtitleBold: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(AppColors.White, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Logo",
                tint = AppColors.Background,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = AppColors.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = subtitleColor,
            fontSize = 16.sp,
            fontWeight = if(isSubtitleBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = AppColors.Gray) },
        placeholder = { Text(text = placeholder, color = AppColors.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.InputBackground,
            unfocusedContainerColor = AppColors.InputBackground,
            focusedTextColor = AppColors.White,
            unfocusedTextColor = AppColors.White,
            cursorColor = AppColors.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation
    )
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonGray,
            disabledContainerColor = AppColors.ButtonGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if(enabled) Color.Black else Color.Gray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun OtpInputField(code: String, onCodeChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = code,
        onValueChange = {
            onCodeChange(it)
            if (it.length == 6) focusManager.clearFocus()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                repeat(6) { index ->
                    val char = if (index < code.length) code[index].toString() else ""
                    val isFocused = code.length == index // Lógica visual para foco se desejar adicionar borda
                    Box(
                        modifier = Modifier
                            .width(45.dp)
                            .height(55.dp)
                            .background(AppColors.InputBackground, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            color = AppColors.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

// ================== Previews ==================
@Preview
@Composable
fun MainPreview() {
    LoginScreen(onNavigate = {})
}