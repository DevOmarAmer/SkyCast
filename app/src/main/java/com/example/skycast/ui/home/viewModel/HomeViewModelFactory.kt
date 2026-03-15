package com.example.skycast.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skycast.data.repository.IWeatherRepository  // ← interface, not concrete class
import com.example.skycast.data.repository.IAIAssistantRepository
import com.example.skycast.utils.SettingsManager
import com.example.skycast.utils.IWidgetUpdaterService
import com.example.skycast.utils.ConnectivityObserver

class HomeViewModelFactory(
    private val repository: IWeatherRepository,
    private val aiRepository: IAIAssistantRepository,
    private val settingsManager: SettingsManager,
    private val widgetUpdaterService: IWidgetUpdaterService,
    private val connectivityObserver: ConnectivityObserver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, aiRepository, settingsManager, widgetUpdaterService, connectivityObserver) as T
        }
        if (modelClass.isAssignableFrom(MorningAnalysisViewModel::class.java)) {
            return MorningAnalysisViewModel(aiRepository, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}