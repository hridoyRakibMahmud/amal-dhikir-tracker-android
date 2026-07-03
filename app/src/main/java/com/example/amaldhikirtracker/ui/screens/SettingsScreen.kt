package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToManageDhikirs: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader("Profile")
            }
            item {
                SettingsMenuItem(
                    title = "Spiritual Profile",
                    subtitle = "Manage your goals and profile details",
                    icon = Icons.Rounded.Person,
                    onClick = { /* Implement later */ }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Preferences")
            }
            item {
                SettingsMenuItem(
                    title = "Calendar",
                    subtitle = "Switch between Gregorian and Hijri",
                    icon = Icons.AutoMirrored.Rounded.List, // Temporary icon or custom
                    onClick = onNavigateToCalendarSettings
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Content")
            }
            item {
                SettingsMenuItem(
                    title = "Manage Dhikirs",
                    subtitle = "Add, edit or remove custom dhikirs",
                    icon = Icons.AutoMirrored.Rounded.List,
                    onClick = onNavigateToManageDhikirs
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("About")
            }
            item {
                SettingsMenuItem(
                    title = "App Version",
                    subtitle = "1.0.0 (Android 16 Ready)",
                    icon = null,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
