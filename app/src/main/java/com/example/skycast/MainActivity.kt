package com.example.skycast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skycast.data.local.WeatherDatabase
import com.example.skycast.data.remote.RetrofitClient // بافتراض أنك أنشأت RetrofitClient سابقاً
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.ui.home.HomeScreen
import com.example.skycast.ui.home.HomeViewModel
import com.example.skycast.ui.home.HomeViewModelFactory
import com.example.skycast.ui.theme.SkyCastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. (Local)
        val database = WeatherDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteLocationDao()

        // 2. (Remote)
        val apiService = RetrofitClient.apiService

        // 3.Repository
        val repository = WeatherRepository(apiService, favoriteDao)

        // 4 Factory
        val factory = HomeViewModelFactory(repository)

        setContent {
            SkyCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val homeViewModel: HomeViewModel = viewModel(factory = factory)


                    homeViewModel.getWeatherData(
                        lat = 30.0444,
                        lon = 31.2357,
                        apiKey = "59ea0a0dbe5f3beceb5f818d109328ec" // استبدل هذا بمفتاح الـ API الخاص بك من OpenWeatherMap
                    )

                    HomeScreen(viewModel = homeViewModel)
                }
            }
        }
    }
}