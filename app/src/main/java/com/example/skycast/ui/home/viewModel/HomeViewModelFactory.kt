package com.example.skycast.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.utils.SettingsManager
import com.example.skycast.utils.IWidgetUpdaterService

import com.example.skycast.utils.ConnectivityObserver

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager,
    private val widgetUpdaterService: IWidgetUpdaterService,
    private val connectivityObserver: ConnectivityObserver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, settingsManager, widgetUpdaterService, connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}