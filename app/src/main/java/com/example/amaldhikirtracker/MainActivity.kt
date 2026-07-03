package com.example.amaldhikirtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
            AmalDhikirTrackerTheme {
                MainScreen(application = application)
            }
        }
    }
}

// Deleted AmalApp() as it is replaced by MainScreen
