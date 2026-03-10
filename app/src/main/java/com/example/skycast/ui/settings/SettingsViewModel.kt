package com.example.skycast.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    val tempUnit = settingsManager.tempUnitFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "metric")
    val windUnit = settingsManager.windUnitFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "m/s")
    val language = settingsManager.langFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "en")
    val locationMethod = settingsManager.locationMethodFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "gps")

    fun saveTempUnit(unit: String) = viewModelScope.launch { settingsManager.saveTempUnit(unit) }
    fun saveWindUnit(unit: String) = viewModelScope.launch { settingsManager.saveWindUnit(unit) }
    fun saveLanguage(lang: String) = viewModelScope.launch { settingsManager.saveLanguage(lang) }
    fun saveLocationMethod(method: String) = viewModelScope.launch { settingsManager.saveLocationMethod(method) }
}