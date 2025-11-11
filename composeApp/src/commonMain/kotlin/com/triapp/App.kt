package com.triapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.triapp.presentation.AppNavigationHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(activity: PlatformContext? = null) {

    MaterialTheme {
        AppNavigationHost(activity = activity)
    }
}