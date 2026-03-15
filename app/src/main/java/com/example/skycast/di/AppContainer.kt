package com.example.skycast.di

import android.content.Context
import com.example.skycast.data.local.LocalDataSource
import com.example.skycast.data.local.db.WeatherDatabase
import com.example.skycast.data.remote.RemoteDataSource
import com.example.skycast.data.remote.ApiService.RetrofitClient
import com.example.skycast.data.repository.AIAssistantRepositoryImpl
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.ui.alerts.viewModel.AlertsViewModelFactory
import com.example.skycast.ui.favorites.SettingsViewModelFactory
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModelFactory
import com.example.skycast.ui.home.viewModel.HomeViewModelFactory
import com.example.skycast.utils.NetworkConnectivityObserver
import com.example.skycast.utils.SettingsManager
import com.example.skycast.utils.WidgetUpdaterServiceImpl
import com.example.skycast.utils.WorkManagerAlertScheduler

class AppContainer(private val context: Context) {
    // Data layer — raw sources
    private val database = WeatherDatabase.getDatabase(context)

    // Data sources
    private val localDataSource = LocalDataSource(
        favoriteDao = database.favoriteLocationDao(),
        alertDao = database.alertDao(),
        weatherDao = database.weatherDao()
    )
    private val remoteDataSource = RemoteDataSource(
        apiService = RetrofitClient.apiService
    )

    // Repository
    private val repository = WeatherRepository(remoteDataSource, localDataSource)

    // Settings & Utils
    val settingsManager = SettingsManager(context)
    private val widgetUpdaterService = WidgetUpdaterServiceImpl(context)
    private val alertScheduler = WorkManagerAlertScheduler(context)
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private val aiRepository = AIAssistantRepositoryImpl()

    // Factories
    val settingsFactory = SettingsViewModelFactory(settingsManager)
    val homeFactory = HomeViewModelFactory(repository, aiRepository, settingsManager, widgetUpdaterService, connectivityObserver)
    val favoritesFactory = FavoritesViewModelFactory(repository, settingsManager)
    val alertsFactory = AlertsViewModelFactory(repository, alertScheduler, settingsManager)
}
