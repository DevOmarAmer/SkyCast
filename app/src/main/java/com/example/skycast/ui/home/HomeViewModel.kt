package com.example.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.utils.Resource
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: IWeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())
    val weatherState: StateFlow<Resource<WeatherResponse>> = _weatherState.asStateFlow()

    // Cache location to auto-refresh on settings change
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentApiKey: String? = null

    init {
        // Observe settings changes
        viewModelScope.launch {
            combine(
                settingsManager.tempUnitFlow,
                settingsManager.langFlow
            ) { unit, lang ->
                Pair(unit, lang)
            }.collectLatest { (unit, lang) ->
                // Re-fetch if location exists
                if (currentLat != null && currentLon != null && currentApiKey != null) {
                    fetchWeatherInternal(currentLat!!, currentLon!!, currentApiKey!!, unit, lang)
                }
            }
        }
    }
    fun getWeatherData(lat: Double, lon: Double, apiKey: String) {
        currentLat = lat
        currentLon = lon
        currentApiKey = apiKey

        viewModelScope.launch {
            val unit = settingsManager.tempUnitFlow.first()
            val lang = settingsManager.langFlow.first()
            fetchWeatherInternal(lat, lon, apiKey, unit, lang)
        }
    }

    private suspend fun fetchWeatherInternal(
        lat: Double,
        lon: Double,
        apiKey: String,
        unit: String,
        lang: String
    ) {
        _weatherState.value = Resource.Loading()
        repository.getWeatherForecast(lat, lon, apiKey, unit, lang).collect { result ->
            _weatherState.value = result
        }
    }
}