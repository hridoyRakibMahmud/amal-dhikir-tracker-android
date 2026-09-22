package com.example.amaldhikirtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.amaldhikirtracker.data.local.ThemeMode
import com.example.amaldhikirtracker.ui.screens.MainScreen
import com.example.amaldhikirtracker.ui.theme.AmalDhikirTrackerTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            // Permission granted, TrackerViewModel will refresh on next init or we can trigger it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestPermissionLauncher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        val application = applicationContext as AmalApplication
        setContent {
            val themeMode by application.preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            AmalDhikirTrackerTheme(darkTheme = resolveDarkTheme(themeMode)) {
                MainScreen(application = application)
            }
        }
    }
}

@Composable
private fun resolveDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
