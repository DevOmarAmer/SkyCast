package com.example.skycast.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.skycast.R
import com.example.skycast.navigation.BottomNavItem
import com.example.skycast.ui.theme.CloudGrey
import com.example.skycast.ui.theme.SkyBlueBright
import com.example.skycast.ui.theme.SkyNavy

@Composable
fun SkyCastBottomNavWidget(navController: NavHostController) {
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
