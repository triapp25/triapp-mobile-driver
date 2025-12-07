package com.triappdriver.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.triappdriver.domain.model.Coordinate

@Composable
expect fun MapViewComponent(
    modifier: Modifier = Modifier,
    coordinate: Coordinate,
    driverCoordinate: Coordinate? = null
)