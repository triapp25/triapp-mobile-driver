package com.triappdriver.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import dev.icerock.moko.permissions.Permission

data class PermissionRequest(
    val permission: Permission,
    val title: String,
    val description: String,
    val icon: ImageVector
)