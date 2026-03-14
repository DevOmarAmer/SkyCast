package com.example.skycast

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skycast.data.local.WeatherDatabase
import com.example.skycast.data.remote.RetrofitClient
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.ui.MainScreen
import com.example.skycast.ui.splash.view.SplashScreen
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.alerts.viewModel.AlertsViewModelFactory
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModelFactory
import com.example.skycast.ui.favorites.SettingsViewModelFactory
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.home.viewModel.HomeViewModelFactory
import com.example.skycast.ui.settings.viewModel.SettingsViewModel
import com.example.skycast.ui.theme.SkyCastTheme
import com.example.skycast.utils.LocationHelper
import com.example.skycast.utils.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge so content goes under status & navigation bars
        enableEdgeToEdge()



        // Data layer
        val database = WeatherDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteLocationDao()
        val weatherDao = database.weatherDao()
        val apiService = RetrofitClient.apiService
        val alertDao = database.alertDao()
        val repository = WeatherRepository(apiService, favoriteDao, alertDao, weatherDao)

        // Settings
        val settingsManager = SettingsManager(applicationContext)
        val widgetUpdaterService = com.example.skycast.utils.WidgetUpdaterServiceImpl(applicationContext)
        val alertScheduler = com.example.skycast.utils.WorkManagerAlertScheduler(applicationContext)
        val connectivityObserver = com.example.skycast.utils.NetworkConnectivityObserver(applicationContext)
        
        val settingsFactory = SettingsViewModelFactory(settingsManager)
        val homeFactory = HomeViewModelFactory(repository, settingsManager, widgetUpdaterService, connectivityObserver)
        val favoritesFactory = FavoritesViewModelFactory(repository)

        val alertsFactory = AlertsViewModelFactory(repository, alertScheduler, settingsManager)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val currentLang by settingsViewModel.language.collectAsStateWithLifecycle()
            var initialLang by remember { mutableStateOf("") }

            LaunchedEffect(currentLang) {
                if (currentLang.isNotEmpty()) {
                    com.example.skycast.utils.LocaleHelper.setLocale(this@MainActivity, currentLang)
                    if (initialLang.isNotEmpty() && initialLang != currentLang) {
                        this@MainActivity.recreate()
                    }
                    if (initialLang.isEmpty()) {
                        initialLang = currentLang
                    }
                }
            }

            SkyCastTheme {
                val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
                val favoritesViewModel: FavoritesViewModel = viewModel(factory = favoritesFactory)
                val weatherState by homeViewModel.weatherState.collectAsStateWithLifecycle()
                val alertsViewModel: AlertsViewModel = viewModel(factory = alertsFactory)

                var minSplashTimeMatured by rememberSaveable { mutableStateOf(false) }
                var locationFetched by rememberSaveable { mutableStateOf(false) }
                var isSplashDismissed by rememberSaveable { mutableStateOf(false) }

                // 1. Ensure splash animations play for at least 2.5s
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2500)
                    minSplashTimeMatured = true
                }

                // 2. The splash is only "done" when the animation finishes AND the initial data arrives.
                // Once dismissed, it will never reappear.
                LaunchedEffect(minSplashTimeMatured, locationFetched, weatherState) {
                    if (minSplashTimeMatured && locationFetched && weatherState !is com.example.skycast.utils.Resource.Loading) {
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
                        LocationHelper.getCurrentLocation(this@MainActivity) { location ->
                            if (location != null) {
                                homeViewModel.getWeatherData(location.latitude, location.longitude,
                                    BuildConfig.API_KEY)
                            } else {
                                homeViewModel.getWeatherData(30.0444, 31.2357, BuildConfig.API_KEY)
                            }
                            locationFetched = true
                        }
                    } else {
                        homeViewModel.getWeatherData(30.0444, 31.2357, BuildConfig.API_KEY)
                        locationFetched = true
                        Toast.makeText(this@MainActivity, "Using default location (Cairo)", Toast.LENGTH_SHORT).show()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        permissions[Manifest.permission.POST_NOTIFICATIONS] != true
                    ) {
                        Toast.makeText(this@MainActivity, "Grant notification permission for alerts", Toast.LENGTH_SHORT).show()
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
                            alertsViewModel = alertsViewModel

                        )
                    }
                }
            } // end SkyCastTheme
        }
    }
}
