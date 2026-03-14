package com.example.skycast.ui.alerts.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.IAlertScheduler
import com.example.skycast.utils.SettingsManager

class AlertsViewModelFactory(
    private val repository: IWeatherRepository,
    private val alertScheduler: IAlertScheduler,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            return AlertsViewModel(repository, alertScheduler, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}