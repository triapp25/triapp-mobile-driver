package com.triapp.presentation.feature.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.triapp.domain.model.SignUpDomainModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignupScreen(onNavigate: () -> Unit) {
    val viewModel = koinViewModel<SignupViewModel>()
    val uiState by viewModel.state.collectAsState()

    val onAction: (SignupIntent) -> Unit = viewModel::processIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SignupEffect.RegisterSuccess -> {
                    println("✅ Registro concluído para ${effect.user.fullName}")
                    onNavigate()
                }

                else -> Unit
            }
        }
    }

    when (uiState.currentStep) {
        RegistrationStep.PersonalInfo -> PersonalInfoScreen(uiState, onAction)
        RegistrationStep.ContactDetails -> ContactDetailsScreen(uiState, onAction)
        RegistrationStep.SecureAccount -> SecureAccountScreen(uiState, onAction)
        RegistrationStep.DocumentVerification -> DocumentVerificationScreen(uiState, onAction)
        RegistrationStep.AllSet -> AllSetScreen(uiState, onAction)
    }
}

@Composable
private fun PersonalInfoScreen(state: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Personal Information", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.fullName,
            onValueChange = { onAction(SignupIntent.EnterFullName(it)) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onAction(SignupIntent.NextStep) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun ContactDetailsScreen(state: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Contact Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(SignupIntent.EnterEmail(it)) },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.phone,
            onValueChange = { onAction(SignupIntent.EnterPhone(it)) },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { onAction(SignupIntent.PreviousStep) }) {
                Text("Back")
            }
            Button(onClick = { onAction(SignupIntent.NextStep) }) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun SecureAccountScreen(state: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Secure your account", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(SignupIntent.EnterPassword(it)) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onAction(SignupIntent.EnterConfirmPassword(it)) },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { onAction(SignupIntent.PreviousStep) }) {
                Text("Back")
            }
            Button(onClick = { onAction(SignupIntent.NextStep) }) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun DocumentVerificationScreen(state: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    var uploadStatus by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<PlatformFile?>(null) }

    val pickerLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Single
    ) { files ->
        selectedFile = files
        uploadStatus = if (selectedFile != null) "Arquivo selecionado: ${selectedFile?.name}" else "Nenhum arquivo selecionado"
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = {
                pickerLauncher.launch()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Profile Photo")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                pickerLauncher.launch()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload ID Document")
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { onAction(SignupIntent.PreviousStep) }) {
                Text("Back")
            }
            Button(onClick = { onAction(SignupIntent.NextStep) }) {
                Text("Finish")
            }
        }
    }
}

@Composable
private fun AllSetScreen(state: SignUpDomainModel, onAction: (SignupIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("All set!", style = MaterialTheme.typography.headlineSmall)
        Text("Your account has been created successfully")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onAction(SignupIntent.NextStep) }) {
            Text("Get Started")
        }
    }
}
