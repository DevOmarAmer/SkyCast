package com.example.skycast.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skycast.BuildConfig
import com.example.skycast.di.AppContainer
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.home.viewModel.MorningAnalysisViewModel
import com.example.skycast.ui.settings.viewModel.SettingsViewModel
import com.example.skycast.ui.splash.view.SplashScreen
import com.example.skycast.ui.theme.SkyCastTheme
import com.example.skycast.utils.LocaleHelper
import com.example.skycast.utils.LocationHelper
import com.example.skycast.utils.Resource
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@Composable
fun SkyCastApp(appContainer: AppContainer) {
    val activity = LocalContext.current as Activity
    val settingsViewModel: SettingsViewModel = viewModel(factory = appContainer.settingsFactory)
    val currentLang by settingsViewModel.language.collectAsStateWithLifecycle()
    var initialLang by remember { mutableStateOf("") }

    LaunchedEffect(currentLang) {
        if (currentLang.isNotEmpty()) {
            LocaleHelper.setLocale(activity, currentLang)
            if (initialLang.isNotEmpty() && initialLang != currentLang) {
                activity.recreate()
            }
            if (initialLang.isEmpty()) {
                initialLang = currentLang
            }
        }
    }

    SkyCastTheme {
        val homeViewModel: HomeViewModel = viewModel(factory = appContainer.homeFactory)
        val favoritesViewModel: FavoritesViewModel = viewModel(factory = appContainer.favoritesFactory)
        val weatherState by homeViewModel.weatherState.collectAsStateWithLifecycle()
        val alertsViewModel: AlertsViewModel = viewModel(factory = appContainer.alertsFactory)
        val morningAnalysisViewModel: MorningAnalysisViewModel = viewModel(factory = appContainer.homeFactory)

        var minSplashTimeMatured by rememberSaveable { mutableStateOf(false) }
        var locationFetched by rememberSaveable { mutableStateOf(false) }
        var isSplashDismissed by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(2500)
            minSplashTimeMatured = true
        }

        LaunchedEffect(minSplashTimeMatured, locationFetched, weatherState) {
            if (minSplashTimeMatured && locationFetched && weatherState !is Resource.Loading) {
                isSplashDismissed = true
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val locationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (locationGranted) {
                LocationHelper.getCurrentLocation(activity) { location ->
                    if (location != null) {
                        homeViewModel.getWeatherData(location.latitude, location.longitude, BuildConfig.API_KEY)
                    } else {
                        homeViewModel.getWeatherData(30.0444, 31.2357, BuildConfig.API_KEY)
                    }
                    locationFetched = true
                }
            } else {
                homeViewModel.getWeatherData(30.0444, 31.2357, BuildConfig.API_KEY)
                locationFetched = true
                Toast.makeText(activity, "Using default location (Cairo)", Toast.LENGTH_SHORT).show()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permissions[Manifest.permission.POST_NOTIFICATIONS] != true
            ) {
                Toast.makeText(activity, "Grant notification permission for alerts", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(perms.toTypedArray())
        }

        AnimatedContent(
            targetState = isSplashDismissed,
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(400))
            },
            label = "splashContent"
        ) { done ->
            if (!done) {
                SplashScreen(onSplashComplete = { /* Now driven entirely by state */ })
            } else {
                MainScreen(
                    homeViewModel = homeViewModel,
                    favoritesViewModel = favoritesViewModel,
                    SettingsViewModel = settingsViewModel,
                    alertsViewModel = alertsViewModel,
                    morningAnalysisViewModel = morningAnalysisViewModel
                )
            }
        }
    }
}
