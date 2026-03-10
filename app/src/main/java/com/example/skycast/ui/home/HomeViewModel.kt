package com.example.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.utils.Resource
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())

    val weatherState: StateFlow<Resource<WeatherResponse>> = _weatherState.asStateFlow()

    fun getWeatherData(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            _weatherState.value = Resource.Loading()

            val currentUnit = settingsManager.tempUnitFlow.first()
            val currentLang = settingsManager.langFlow.first()

            repository.getWeatherForecast(lat, lon, apiKey, currentUnit, currentLang).collect { result ->
                _weatherState.value = result
            }
        }
    }
}