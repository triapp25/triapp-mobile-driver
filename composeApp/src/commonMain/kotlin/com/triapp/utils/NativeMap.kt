package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.triapp.presentation.feature.home.MapController

@Composable
expect fun NativeMap(
    modifier: Modifier = Modifier,
    onMapReady: (PlatformMap) -> Unit,
    mapController: MapController
)
