package com.bindraft.amaldhikirtracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.bindraft.amaldhikirtracker.R

sealed class Screen(
    val route: String,
    @StringRes val labelRes: Int? = null,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null
) {
    object Tracker : Screen(
        route = "tracker",
        labelRes = R.string.nav_home,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Rounded.Home
    )

    object Counter : Screen("counter/{dhikirId}") {
        fun createRoute(dhikirId: Long) = "counter/$dhikirId"
    }

    object History : Screen(
        route = "history",
        labelRes = R.string.nav_history,
        icon = Icons.Outlined.History,
        selectedIcon = Icons.Rounded.History
    )

    object ManageDhikirs : Screen("manage_dhikirs")

    object Settings : Screen(
        route = "settings",
        labelRes = R.string.nav_settings,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings
    )

    object Login : Screen("login")

    object Fasting : Screen("fasting")

    object Quran : Screen("quran")

    companion object {
        val bottomNavItems = listOf(Tracker, History, Settings)
    }
}
