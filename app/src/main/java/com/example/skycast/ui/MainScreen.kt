package com.example.skycast.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.components.SkyCastBottomNavWidget
import com.example.skycast.ui.components.SkyCastNavGraphWidget
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.home.viewModel.MorningAnalysisViewModel
import com.example.skycast.ui.settings.viewModel.SettingsViewModel

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    SettingsViewModel: SettingsViewModel,
    alertsViewModel: AlertsViewModel,
    morningAnalysisViewModel: MorningAnalysisViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where we hide the bottom nav (detail/add screens)
    val hideNavRoutes = setOf("add_favorite", "favorite_detail", "map_location_picker", "morning_analysis")
    val showBottomNav = currentRoute !in hideNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        SkyCastNavGraphWidget(
            navController = navController,
            homeViewModel = homeViewModel,
            favoritesViewModel = favoritesViewModel,
            settingsViewModel = SettingsViewModel,
            alertsViewModel = alertsViewModel,
            morningAnalysisViewModel = morningAnalysisViewModel,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            enter = slideInVertically(tween(250)) { it } + fadeIn(tween(250)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
        ) {
            SkyCastBottomNavWidget(navController = navController)
        }
    }
}