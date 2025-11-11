package com.triapp.presentation.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triapp.domain.model.LoginDomainModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(activity: Any?, onNavigate: () -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    val uiState by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            when (uiState.step) {
                LoginStep.Login -> LoginContent(uiState, activity, viewModel::processIntent)
                LoginStep.ResetCode -> ResetCodeContent(uiState, viewModel::processIntent)
            }

            if (showForgotPassword) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.processIntent(LoginIntent.DismissForgotPassword) },
                    sheetState = sheetState
                ) {
                    ForgotPasswordModal(viewModel::processIntent)
                }
            }
        }
    }
}

@Composable
fun LoginContent(state: LoginDomainModel, activity: Any?, onAction: (LoginIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Ride", style = MaterialTheme.typography.headlineSmall)
        Text("Urban mobility reimagined", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        TextField(
            value = state.phone,
            onValueChange = { onAction(LoginIntent.EnterPhone(it)) },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onAction(LoginIntent.SubmitLogin(activity)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onAction(LoginIntent.GoToSignup) }) {
            Text("SignUp")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onAction(LoginIntent.ForgotPassword) }) {
            Text("Forgot password?")
        }
    }
}


@Composable
fun ForgotPasswordModal(onAction: (LoginIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
        Text("Forgot password?", style = MaterialTheme.typography.headlineSmall)
        Text("No worries, we'll send you reset instructions")
        Spacer(Modifier.height(16.dp))

        var email by remember { mutableStateOf("") }

        TextField(
            value = email,
            onValueChange = {
                email = it
                onAction(LoginIntent.EnterEmail(it))
            },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onAction(LoginIntent.SendResetCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send reset code")
        }
    }
}


@Composable
fun ResetCodeContent(state: LoginDomainModel, onAction: (LoginIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Enter Reset Code", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Enter the code you received from Firebase")

        Spacer(Modifier.height(24.dp))
        TextField(
            value = state.resetCode,
            onValueChange = { onAction(LoginIntent.EnterResetCode(it)) },
            label = { Text("Verification code") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onAction(LoginIntent.VerifyResetCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Code")
        }

        TextButton(onClick = { onAction(LoginIntent.BackToLogin) }) {
            Text("Back to login")
        }
    }
}