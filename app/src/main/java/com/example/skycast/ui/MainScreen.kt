package com.example.skycast.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skycast.navigation.BottomNavItem
import com.example.skycast.ui.favorites.FavoritesScreen
import com.example.skycast.ui.favorites.FavoritesViewModel
import com.example.skycast.ui.home.HomeScreen
import com.example.skycast.ui.home.HomeViewModel

@Composable
fun MainScreen(homeViewModel: HomeViewModel, favoritesViewModel: FavoritesViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(viewModel = homeViewModel)
            }
            composable(BottomNavItem.Favorites.route) {
                FavoritesScreen( viewModel = favoritesViewModel, onNavigateToAddPlace = {
                    //TODO: navigate to add place screen
                })

            }
            composable(BottomNavItem.Alerts.route) {
                AlertsScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Alerts,
        BottomNavItem.Settings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true // يمنع تكرار نفس الصفحة عند الضغط على الزر مرتين
                        restoreState = true
                    }
                }
            )
        }
    }
}