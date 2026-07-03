package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.amaldhikirtracker.AmalApplication
import com.example.amaldhikirtracker.ui.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.amaldhikirtracker.ui.viewmodel.CounterViewModel
import com.example.amaldhikirtracker.ui.viewmodel.HistoryViewModel
import com.example.amaldhikirtracker.ui.viewmodel.TrackerViewModel

@Composable
fun MainScreen(
    application: AmalApplication
) {
    val navController = rememberNavController()
    val trackerViewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(
            application.repository, 
            application.preferencesManager,
            application.locationTracker
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if the current destination is a top-level screen
    val showBottomBar = Screen.bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets(0, 0, 0, 0) // Explicitly handle insets if needed, or let Scaffold do it
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) 
                                        screen.selectedIcon!! else screen.icon!!, 
                                    contentDescription = screen.label 
                                ) 
                            },
                            label = { Text(screen.label!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(
                navController = navController, 
                startDestination = Screen.Tracker.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Tracker.route) {
                    TrackerScreen(
                        viewModel = trackerViewModel,
                        onNavigateToCounter = { dhikirId ->
                            navController.navigate(Screen.Counter.createRoute(dhikirId))
                        }
                    )
                }
                composable(
                    route = Screen.Counter.route,
                    arguments = listOf(navArgument("dhikirId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val dhikirId = backStackEntry.arguments?.getLong("dhikirId") ?: 0L
                    val counterViewModel: CounterViewModel = viewModel(
                        factory = CounterViewModel.Factory(application.repository, dhikirId)
                    )
                    CounterScreen(
                        viewModel = counterViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.History.route) {
                    val historyViewModel: HistoryViewModel = viewModel(
                        factory = HistoryViewModel.Factory(
                            application.repository,
                            application.preferencesManager,
                            application.locationTracker
                        )
                    )
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        isTopLevel = true
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToManageDhikirs = {
                            navController.navigate(Screen.ManageDhikirs.route)
                        },
                        onNavigateToCalendarSettings = {
                            navController.navigate(Screen.CalendarSettings.route)
                        }
                    )
                }
                composable(Screen.CalendarSettings.route) {
                    CalendarSettingsScreen(
                        preferencesManager = application.preferencesManager,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.ManageDhikirs.route) {
                    ManageDhikirsScreen(
                        viewModel = trackerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
