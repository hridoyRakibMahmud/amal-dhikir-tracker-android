package com.bindraft.amaldhikirtracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.bindraft.amaldhikirtracker.data.local.ThemeMode
import com.bindraft.amaldhikirtracker.reminder.ReminderScheduler
import com.bindraft.amaldhikirtracker.ui.screens.MainScreen
import com.bindraft.amaldhikirtracker.ui.theme.AmalDhikirTrackerTheme

/**
 * Extends AppCompatActivity (not plain ComponentActivity) specifically so
 * AppCompatDelegate.setApplicationLocales() recreates this activity and applies the chosen
 * app language — that auto-recreate hook only reliably fires for AppCompatActivity.
 */
class MainActivity : AppCompatActivity() {
    private val pendingReminderTypeState = mutableStateOf<String?>(null)

    override fun onStart() {
        super.onStart()
        (application as AmalApplication).spiritualDay.onForeground()
    }

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

        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += android.Manifest.permission.POST_NOTIFICATIONS
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        pendingReminderTypeState.value = intent?.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TYPE)

        val application = applicationContext as AmalApplication
        setContent {
            val themeMode by application.preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val pendingReminderType by pendingReminderTypeState
            AmalDhikirTrackerTheme(darkTheme = resolveDarkTheme(themeMode)) {
                MainScreen(
                    application = application,
                    pendingReminderType = pendingReminderType,
                    onConsumePendingReminder = { pendingReminderTypeState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingReminderTypeState.value = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TYPE)
    }
}

@Composable
private fun resolveDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
