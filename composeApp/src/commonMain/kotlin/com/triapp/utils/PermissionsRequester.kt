package com.triapp.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.triapp.domain.model.PermissionRequest
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch

@Composable
fun PermissionsRequester(
    permissions: List<PermissionRequest>,
    onAllPermissionsGranted: () -> Unit = {},
    content: @Composable (controller: PermissionsController) -> Unit
) {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val scope = rememberCoroutineScope()

    BindEffect(controller)

    var permissionStates by remember {
        mutableStateOf(permissions.associate { it.permission to PermissionState.NotDetermined })
    }

    var permissionDeniedAlways by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        permissionStates = permissions.associate { req ->
            req.permission to controller.getPermissionState(req.permission)
        }
    }

    val allGranted = permissionStates.values.all { it == PermissionState.Granted }
    val anyPermanentlyDenied = permissionStates.values.any { it == PermissionState.DeniedAlways }

    when {
        allGranted -> {
            LaunchedEffect(Unit) {
                onAllPermissionsGranted()
            }
            content(controller)
        }

        anyPermanentlyDenied || permissionDeniedAlways -> {
            PermissionsDeniedScreen(
                permissions = permissions,
                permissionStates = permissionStates,
                onOpenSettings = {
                    controller.openAppSettings()
                }
            )
        }

        else -> {
            PermissionsRequestScreen(
                permissions = permissions,
                permissionStates = permissionStates,
                onRequestPermissions = {
                    scope.launch {
                        permissions.forEach { req ->
                            if (permissionStates[req.permission] != PermissionState.Granted) {
                                try {
                                    controller.providePermission(req.permission)
                                } catch (e: DeniedAlwaysException) {
                                    permissionDeniedAlways = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        permissionStates = permissions.associate { req ->
                            req.permission to controller.getPermissionState(req.permission)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionsRequestScreen(
    permissions: List<PermissionRequest>,
    permissionStates: Map<Permission, PermissionState>,
    onRequestPermissions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "The app needs a few permissions to work properly.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                permissions.forEach { req ->
                    PermissionItem(
                        title = req.title,
                        description = req.description,
                        icon = req.icon,
                        isGranted = permissionStates[req.permission] == PermissionState.Granted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Allow All Access")
                }
            }
        }
    }
}
@Composable
private fun PermissionsDeniedScreen(
    permissions: List<PermissionRequest>,
    permissionStates: Map<Permission, PermissionState>,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Access Denied",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = "Some permissions have been denied. Enable the following permissions in the app settings",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                permissions.forEach { req ->
                    val state = permissionStates[req.permission]
                    if (state == PermissionState.DeniedAlways || state == PermissionState.Denied) {
                        PermissionItem(
                            title = req.title,
                            description = req.description,
                            icon = req.icon,
                            isGranted = false,
                            isDenied = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Setting")
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isDenied: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isGranted -> MaterialTheme.colorScheme.primaryContainer
                isDenied -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isGranted) {
                    MaterialTheme.colorScheme.primary
                } else if (isDenied) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}