package com.triappdriver.presentation.feature.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triappdriver.TriColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ================== Tela Principal ==================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    activity: Any?,
    onNavigateToSignup: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is LoginEffect.ShowError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is LoginEffect.NavigateToSignup -> onNavigateToSignup()
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                else -> Unit
            }
        }
    }

    if (uiState.isLoading) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(Modifier.size(100.dp))
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
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
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                }
            ) { step ->
                when (step) {
                    LoginStep.Login -> RideLoginScreen(
                        onLoginClick = { phone ->
                            viewModel.processIntent(
                                LoginIntent.SubmitLogin(
                                    activity = activity,
                                    phone
                                )
                            )
                        },
                        onSignUpClick = { viewModel.processIntent(LoginIntent.NavigateToSignup) }
                    )

                    LoginStep.ResetCode -> SmsVerificationScreen(
                        phoneNumber = uiState.phone,
                        onVerifyClick = { code ->
                            viewModel.processIntent(
                                LoginIntent.VerifyCode(
                                    code
                                )
                            )
                        },
                        onResendClick = { viewModel.processIntent(LoginIntent.SendResetCode(activity)) }
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
            title = "Driver",
            subtitle = "Mobilidade que conecta pessoas e lugares",
            icon = Icons.Outlined.LocationOn
        )

        // Card Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bem vindo de volta",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Entre com seu número de telefone para continuar",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it.filter(Char::isDigit).take(11) },
                    visualTransformation = PhoneNumberVisualTransformation(),
                    placeholder = "(11) 99999-9999",
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(text = "Continar", onClick = { onLoginClick(phoneNumber) })

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                val signUpText = buildAnnotatedString {
                    append("Você é novo aqui? ")
                    pushStringAnnotation(tag = "signup", annotation = "signup")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Criar uma conta")
                    }
                    pop()
                }

                ClickableText(
                    text = signUpText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                    onClick = { offset ->
                        signUpText.getStringAnnotations(
                            tag = "signup",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { onSignUpClick() }
                    }
                )
            }
        }

        // Footer
        val termsText = buildAnnotatedString {
            append("Para saber mais, leia nossos ")
            pushStringAnnotation(tag = "Termos", annotation = "Termos")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("Termos de Serviço")
            }
            pop()
        }

        ClickableText(
            text = termsText,
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            ),
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
fun AppLogo(
    title: String,
    subtitle: String,
    icon: ImageVector,
    subtitleColor: Color = MaterialTheme.colorScheme.secondary,
    isSubtitleBold: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = subtitleColor,
            fontSize = 16.sp,
            fontWeight = if (isSubtitleBold) FontWeight.Bold else FontWeight.Normal,
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
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.secondary) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.primary,
            unfocusedTextColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
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
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = TriColors.ButtonGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
    ) {
        Text(
            text = text,
            color = if (enabled) Color.Black else Color.Gray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun OtpInputField(
    code: String,
    onCodeChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = code,
        onValueChange = {
            if (it.length <= 6) onCodeChange(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.White.copy(alpha = 0.8f)), // pequeno cursor, não invisível
        interactionSource = interactionSource,
        textStyle = LocalTextStyle.current.copy( // textStyle obrigatório
            color = Color.Transparent // texto invisível, já que mostramos nas caixinhas
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { /* não use focusable() */ },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->

                    val isBoxFocused = isFocused && code.length == index

                    Box(
                        modifier = Modifier
                            .size(width = 45.dp, height = 55.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isBoxFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                focusRequester.requestFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (index < code.length) code[index].toString() else "",
                            color = MaterialTheme.colorScheme.primary,
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

class PhoneNumberVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(11)

        val formatted = buildString {
            if (raw.isNotEmpty()) append("(")
            if (raw.length >= 1) append(raw.substring(0, 1))
            if (raw.length >= 2) append(raw.substring(1, 2))
            if (raw.length >= 2) append(") ")

            if (raw.length >= 3) append(raw.substring(2, minOf(7, raw.length)))
            if (raw.length >= 7) append("-")
            if (raw.length >= 7) append(raw.substring(7))
        }

        val originalToTransformed = IntArray(raw.length) { -1 }
        var rawIndex = 0

        formatted.forEachIndexed { index, c ->
            if (c.isDigit()) {
                originalToTransformed[rawIndex] = index
                rawIndex++
            }
        }

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset < 0 -> 0
                    offset >= raw.length -> formatted.length
                    else -> originalToTransformed[offset]
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= formatted.length) return raw.length

                // encontra o último dígito antes do cursor
                for (i in originalToTransformed.indices.reversed()) {
                    if (originalToTransformed[i] <= offset) return i + 1
                }
                return 0
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}


// ================== Previews ==================
@Preview
@Composable
fun MainPreview() {
    LoginScreen(null, {}, {})
}