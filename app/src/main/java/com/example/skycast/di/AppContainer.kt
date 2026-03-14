package com.example.skycast.di

import android.content.Context
import com.example.skycast.data.local.WeatherDatabase
import com.example.skycast.data.remote.RetrofitClient
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
    // Data layer
    private val database = WeatherDatabase.getDatabase(context)
    private val favoriteDao = database.favoriteLocationDao()
    private val weatherDao = database.weatherDao()
    private val apiService = RetrofitClient.apiService
    private val alertDao = database.alertDao()
    private val repository = WeatherRepository(apiService, favoriteDao, alertDao, weatherDao)

    // Settings & Utils
    val settingsManager = SettingsManager(context)
    private val widgetUpdaterService = WidgetUpdaterServiceImpl(context)
    private val alertScheduler = WorkManagerAlertScheduler(context)
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private val aiRepository = AIAssistantRepositoryImpl()
    
    // Factories
    val settingsFactory = SettingsViewModelFactory(settingsManager)
    val homeFactory = HomeViewModelFactory(repository, aiRepository, settingsManager, widgetUpdaterService, connectivityObserver)
    val favoritesFactory = FavoritesViewModelFactory(repository)
    val alertsFactory = AlertsViewModelFactory(repository, alertScheduler, settingsManager)
}
