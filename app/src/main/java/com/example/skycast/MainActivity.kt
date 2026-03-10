package com.example.skycast

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skycast.data.local.WeatherDatabase
import com.example.skycast.data.remote.RetrofitClient // بافتراض أنك أنشأت RetrofitClient سابقاً
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.ui.MainScreen
import com.example.skycast.ui.favorites.FavoritesViewModel
import com.example.skycast.ui.favorites.FavoritesViewModelFactory
import com.example.skycast.ui.favorites.SettingsViewModelFactory
import com.example.skycast.ui.home.HomeViewModel
import com.example.skycast.ui.home.HomeViewModelFactory
import com.example.skycast.ui.settings.SettingsViewModel
import com.example.skycast.ui.theme.SkyCastTheme
import com.example.skycast.utils.LocationHelper
import com.example.skycast.utils.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. (Local)
        val database = WeatherDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteLocationDao()
        val API_KEY : String  = "59ea0a0dbe5f3beceb5f818d109328ec"
        // 2. (Remote)
        val apiService = RetrofitClient.apiService

        // 3.Repository
        val repository = WeatherRepository(apiService, favoriteDao)

        // 4 Factory
        val homeFactory = HomeViewModelFactory(repository)

        val favoritesFactory = FavoritesViewModelFactory(repository)
        val settingsManager = SettingsManager(applicationContext)
        val settingsFactory = SettingsViewModelFactory(settingsManager)


        setContent {
            SkyCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
                    val favoritesViewModel: FavoritesViewModel = viewModel(factory = favoritesFactory)
                    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
                    var locationFetched by remember { mutableStateOf(false) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                        if (isGranted) {
                            LocationHelper.getCurrentLocation(context) { location ->
                                if (location != null) {
                                    homeViewModel.getWeatherData(
                                        lat = location.latitude,
                                        lon = location.longitude,
                                        apiKey = API_KEY
                                    )
                                } else {
                                    homeViewModel.getWeatherData(30.0444, 31.2357, API_KEY)
                                }
                                locationFetched = true
                            }
                        } else {
                            homeViewModel.getWeatherData(30.0444, 31.2357, API_KEY)
                            locationFetched = true
                        }
                    }

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }

                    if (!locationFetched) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        MainScreen(homeViewModel = homeViewModel, favoritesViewModel = favoritesViewModel, SettingsViewModel = settingsViewModel )                    }
                }
            }
        }
    }
}