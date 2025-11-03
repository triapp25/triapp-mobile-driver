package com.triapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mapbox.common.MapboxOptions
import com.triapp.presentation.AppNavigationHost
import com.triapp.utils.registerCurrentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        registerCurrentActivity(this)
        MapboxOptions.accessToken = "sk.eyJ1Ijoia3Nkcm9mNTAwIiwiYSI6ImNtaGd5ZHhtMjBrb24ycnB5Z3hmaDQxaGMifQ.rg9-3efU32R3dCv-ahAPJw"

        super.onCreate(savedInstanceState)
        var isChecking = true
        lifecycleScope.launch {
            delay(1000)
            isChecking = false
        }
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                isChecking
            }
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "Notificações desativadas", Toast.LENGTH_SHORT).show()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AppNavigationHost(activity = this@MainActivity)
        }
    }
}