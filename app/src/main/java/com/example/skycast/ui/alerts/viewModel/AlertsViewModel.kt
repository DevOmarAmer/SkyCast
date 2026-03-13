package com.example.skycast.ui.alerts.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.IAlertScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: IWeatherRepository,
    private val alertScheduler: IAlertScheduler
) : ViewModel() {

    val alertsList: StateFlow<List<WeatherAlert>> = repository.getAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun scheduleAlert(delayMinutes: Long, type: String) {
        val alert = alertScheduler.schedule(delayMinutes, type)
        viewModelScope.launch { repository.insertAlert(alert) }
    }

    fun deleteAlert(alert: WeatherAlert) {
        alertScheduler.cancel(alert.workerId)
        viewModelScope.launch { repository.deleteAlert(alert) }
    }
}