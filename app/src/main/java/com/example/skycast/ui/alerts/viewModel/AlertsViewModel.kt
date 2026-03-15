package com.example.skycast.ui.alerts.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.IAlertScheduler
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: IWeatherRepository,
    private val alertScheduler: IAlertScheduler,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val alertsList: StateFlow<List<WeatherAlert>> = repository.getAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val morningBriefEnabled: StateFlow<Boolean> = settingsManager.morningBriefEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val morningBriefHour: StateFlow<Int> = settingsManager.morningBriefHourFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    val morningBriefMinute: StateFlow<Int> = settingsManager.morningBriefMinuteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Condition Alerts ────────────────────────────────────────────────────────

    fun scheduleConditionAlert(
        conditionType: String,
        threshold: Double,
        alertType: String,
        label: String,
        lat: Double,
        lon: Double,
        apiKey: String,
        startDateTime: Long,
        endDateTime: Long
    ) {
        val alert = alertScheduler.scheduleConditionAlert(
            conditionType = conditionType,
            threshold     = threshold,
            alertType     = alertType,
            label         = label,
            lat           = lat,
            lon           = lon,
            apiKey        = apiKey,
            startDateTime = startDateTime,
            endDateTime   = endDateTime
        )
        viewModelScope.launch { repository.insertAlert(alert) }
    }

    fun deleteAlert(alert: WeatherAlert) {
        alertScheduler.cancel(alert.workerId)
        viewModelScope.launch { repository.deleteAlert(alert) }
    }

    // ── Morning Brief ───────────────────────────────────────────────────────────

    fun setMorningBriefEnabled(
        enabled: Boolean,
        hour: Int,
        minute: Int,
        lat: Double,
        lon: Double,
        apiKey: String
    ) {
        viewModelScope.launch {
            settingsManager.saveMorningBriefEnabled(enabled)
            if (enabled) {
                val workerId = alertScheduler.scheduleMorningBrief(hour, minute, lat, lon, apiKey)
                settingsManager.saveMorningBriefWorkerId(workerId)
            } else {
                alertScheduler.cancelMorningBrief()
                settingsManager.saveMorningBriefWorkerId("")
            }
        }
    }

    fun updateMorningBriefTime(
        hour: Int,
        minute: Int,
        lat: Double,
        lon: Double,
        apiKey: String
    ) {
        viewModelScope.launch {
            settingsManager.saveMorningBriefTime(hour, minute)
            // Only re-schedule if it's currently enabled
            if (settingsManager.morningBriefEnabledFlow.first()) {
                alertScheduler.scheduleMorningBrief(hour, minute, lat, lon, apiKey)
            }
        }
    }
}