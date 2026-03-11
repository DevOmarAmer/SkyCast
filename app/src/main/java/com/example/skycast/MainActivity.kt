package com.example.skycast

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skycast.data.local.WeatherDatabase
import com.example.skycast.data.remote.RetrofitClient
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.ui.MainScreen
import com.example.skycast.ui.SplashScreen
import com.example.skycast.ui.favorites.FavoritesViewModel
import com.example.skycast.ui.favorites.FavoritesViewModelFactory
import com.example.skycast.ui.favorites.SettingsViewModelFactory
import com.example.skycast.ui.home.HomeViewModel
import com.example.skycast.ui.home.HomeViewModelFactory
import com.example.skycast.ui.settings.SettingsViewModel
import com.example.skycast.ui.theme.SkyCastTheme
import com.example.skycast.ui.theme.SkyBlueBright
import com.example.skycast.utils.LocationHelper
import com.example.skycast.utils.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge so content goes under status & navigation bars
        enableEdgeToEdge()

        val apiKey = "59ea0a0dbe5f3beceb5f818d109328ec"

        // Data layer
        val database = WeatherDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteLocationDao()
        val apiService = RetrofitClient.apiService
        val repository = WeatherRepository(apiService, favoriteDao)

        // Settings
        val settingsManager = SettingsManager(applicationContext)
        val settingsFactory = SettingsViewModelFactory(settingsManager)
        val homeFactory = HomeViewModelFactory(repository, settingsManager)
        val favoritesFactory = FavoritesViewModelFactory(repository)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val currentLang by settingsViewModel.language.collectAsState()
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
                var splashDone by remember { mutableStateOf(false) }

                AnimatedContent(
                    targetState = splashDone,
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(400))
                    },
                    label = "splashContent"
                ) { done ->
                    if (!done) {
                        SplashScreen(onSplashComplete = { splashDone = true })
                    } else {
                        val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
                        val favoritesViewModel: FavoritesViewModel = viewModel(factory = favoritesFactory)
                        var locationFetched by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val locationGranted =
                        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                    if (locationGranted) {
                        LocationHelper.getCurrentLocation(this@MainActivity) { location ->
                            if (location != null) {
                                homeViewModel.getWeatherData(location.latitude, location.longitude, apiKey)
                            } else {
                                homeViewModel.getWeatherData(30.0444, 31.2357, apiKey)
                            }
                            locationFetched = true
                        }
                    } else {
                        homeViewModel.getWeatherData(30.0444, 31.2357, apiKey)
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

                if (!locationFetched) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SkyBlueBright)
                    }
                } else {
                    MainScreen(
                        homeViewModel = homeViewModel,
                        favoritesViewModel = favoritesViewModel,
                        SettingsViewModel = settingsViewModel
                    )
                    } // end if locationFetched
                    } // end else (splashDone)
                } // end AnimatedContent
            } // end SkyCastTheme
        }
    }
}