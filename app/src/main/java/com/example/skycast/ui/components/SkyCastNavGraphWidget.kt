package com.example.skycast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.skycast.BuildConfig
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.navigation.BottomNavItem
import com.example.skycast.ui.alerts.view.AlertsScreen
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.favorites.view.AddFavoriteScreen
import com.example.skycast.ui.favorites.view.FavoriteDetailScreen
import com.example.skycast.ui.favorites.view.FavoritesScreen
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.home.view.HomeScreen
import com.example.skycast.ui.home.view.MorningAIAnalysisScreen
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.home.viewModel.MorningAnalysisViewModel
import com.example.skycast.ui.settings.view.MapLocationPickerScreen
import com.example.skycast.ui.settings.view.SettingsScreen
import com.example.skycast.ui.settings.viewModel.SettingsViewModel
import com.example.skycast.utils.Resource
import kotlinx.coroutines.launch

@Composable
fun SkyCastNavGraphWidget(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    settingsViewModel: SettingsViewModel,
    alertsViewModel: AlertsViewModel,
    morningAnalysisViewModel: MorningAnalysisViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToAnalysis = {
                    navController.navigate("morning_analysis")
                }
            )
        }
        composable(BottomNavItem.Favorites.route) {
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onNavigateToAddPlace = { navController.navigate("add_favorite") },
                onNavigateToDetail = { location ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("fav_location", location)
                    navController.navigate("favorite_detail")
                }
            )
        }
        composable(BottomNavItem.Alerts.route) {
            // Extract plain values from HomeViewModel here in the nav layer
            // AlertsScreen and AlertsViewModel must not depend on HomeViewModel.
            val location by homeViewModel.currentLocation.collectAsStateWithLifecycle()
            val apiKey by homeViewModel.exposedApiKey.collectAsStateWithLifecycle()

            AlertsScreen(
                viewModel = alertsViewModel,
                location = location,
                apiKey = apiKey
            )
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onOpenMap = { navController.navigate("map_location_picker") }
            )
        }
        composable("add_favorite") {
            AddFavoriteScreen(
                viewModel = favoritesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("favorite_detail") {
            val location = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<FavoriteLocation>("fav_location")

            if (location != null) {
                FavoriteDetailScreen(
                    location = location,
                    viewModel = favoritesViewModel,
                    apiKey = BuildConfig.API_KEY,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable("map_location_picker") {
            val coroutineScope = rememberCoroutineScope()
            val apiKey = BuildConfig.API_KEY
            MapLocationPickerScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLocationSet = { lat, lon, city ->
                    coroutineScope.launch {
                        settingsViewModel.saveMapLocation(lat, lon)
                    }
                    homeViewModel.getWeatherData(lat, lon, apiKey)
                    navController.popBackStack()
                }
            )
        }
        composable("morning_analysis") {
            val weatherState by homeViewModel.weatherState.collectAsState()
            val data = (weatherState as? Resource.Success)?.data

            if (data != null) {
                LaunchedEffect(Unit) {
                    morningAnalysisViewModel.fetchDetailedAnalysis(data.city.name, data.forecastList)
                }
                MorningAIAnalysisScreen(
                    viewModel = morningAnalysisViewModel,
                    cityName = data.city.name,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
