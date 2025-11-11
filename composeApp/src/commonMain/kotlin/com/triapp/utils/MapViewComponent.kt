package com.triapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.triapp.domain.model.Coordinate

@Composable
expect fun MapViewComponent(
    modifier: Modifier = Modifier,
    coordinate: Coordinate
)