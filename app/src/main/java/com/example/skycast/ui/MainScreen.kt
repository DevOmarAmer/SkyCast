package com.example.skycast.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skycast.R
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.navigation.BottomNavItem
import com.example.skycast.ui.alerts.view.AlertsScreen
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.favorites.view.AddFavoriteScreen
import com.example.skycast.ui.favorites.view.FavoriteDetailScreen
import com.example.skycast.ui.favorites.view.FavoritesScreen
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.home.view.HomeScreen
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.settings.view.MapLocationPickerScreen
import com.example.skycast.ui.settings.view.SettingsScreen
import com.example.skycast.ui.settings.viewModel.SettingsViewModel
import com.example.skycast.ui.theme.*

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    SettingsViewModel: SettingsViewModel,
    alertsViewModel: AlertsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where we hide the bottom nav (detail/add screens)
    val hideNavRoutes = setOf("add_favorite", "favorite_detail", "map_location_picker")
    val showBottomNav = currentRoute !in hideNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(viewModel = homeViewModel)
                }
                composable(BottomNavItem.Favorites.route) {
                    FavoritesScreen(
                        viewModel = favoritesViewModel,
                        onNavigateToAddPlace = { navController.navigate("add_favorite") },
                        onNavigateToDetail = { location ->
                            // Encode city name as nav arg
                            navController.currentBackStackEntry?.savedStateHandle?.set("fav_location", location)
                            navController.navigate("favorite_detail")
                        }
                    )
                }
                composable(BottomNavItem.Alerts.route) {
                    AlertsScreen(viewModel = alertsViewModel)
                }
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        viewModel = SettingsViewModel,
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
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
                composable("map_location_picker") {
                    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                    val apiKey = com.example.skycast.BuildConfig.API_KEY
                    MapLocationPickerScreen(
                        viewModel = SettingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLocationSet = { lat, lon, city ->
                            coroutineScope.launch {
                                SettingsViewModel.saveMapLocation(lat, lon)
                            }
                            homeViewModel.getWeatherData(lat, lon, apiKey)
                            navController.popBackStack()
                        }
                    )
                }
        }

        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            enter = slideInVertically(tween(250)) { it } + fadeIn(tween(250)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
        ) {
            SkyCastBottomNav(navController = navController)
        }
    }
}

@Composable
fun SkyCastBottomNav(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Alerts,
        BottomNavItem.Settings
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp)),
        containerColor = SkyNavy.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val itemTitle = when(item.title) {
                "Home" -> stringResource(R.string.nav_home)
                "Favorites" -> stringResource(R.string.nav_favorites)
                "Alerts" -> stringResource(R.string.nav_alerts)
                "Settings" -> stringResource(R.string.nav_settings)
                else -> item.title
            }
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = itemTitle
                    )
                },
                label = { Text(itemTitle, style = MaterialTheme.typography.labelSmall) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SkyBlueBright,
                    selectedTextColor = SkyBlueBright,
                    unselectedIconColor = CloudGrey,
                    unselectedTextColor = CloudGrey,
                    indicatorColor = SkyBlueBright.copy(alpha = 0.15f)
                )
            )
        }
    }
}