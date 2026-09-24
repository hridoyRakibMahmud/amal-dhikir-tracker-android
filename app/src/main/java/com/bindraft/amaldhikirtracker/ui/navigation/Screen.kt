package com.bindraft.amaldhikirtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String? = null,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null
) {
    object Tracker : Screen(
        route = "tracker",
        label = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Rounded.Home
    )

    object Counter : Screen("counter/{dhikirId}") {
        fun createRoute(dhikirId: Long) = "counter/$dhikirId"
    }

    object History : Screen(
        route = "history",
        label = "History",
        icon = Icons.Outlined.History,
        selectedIcon = Icons.Rounded.History
    )

    object ManageDhikirs : Screen("manage_dhikirs")

    object Settings : Screen(
        route = "settings",
        label = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings
    )

    object Login : Screen("login")

    object Fasting : Screen("fasting")

    companion object {
        val bottomNavItems = listOf(Tracker, History, Settings)
    }
}
