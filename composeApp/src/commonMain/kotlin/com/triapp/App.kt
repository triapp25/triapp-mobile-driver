package com.triapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.triapp.presentation.AppNavigationHost
import com.triapp.utils.FirebaseServiceImpl
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(activity: PlatformContext? = null) {

    val authManager = remember { FirebaseServiceImpl() }

    TriAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            AppNavigationHost(
                activity = activity,
                authManager = authManager
            )
        }
    }
}