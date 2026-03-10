package com.example.skycast.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skycast.ui.settings.SettingsViewModel
import com.example.skycast.utils.SettingsManager

class SettingsViewModelFactory(
    private val settingsManager : SettingsManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel( settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}