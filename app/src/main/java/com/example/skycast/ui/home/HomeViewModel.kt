package com.example.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.WeatherRepository
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())

    val weatherState: StateFlow<Resource<WeatherResponse>> = _weatherState.asStateFlow()

    fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String = "metric", lang: String = "en") {
        viewModelScope.launch {
            _weatherState.value = Resource.Loading()

            repository.getWeatherForecast(lat, lon, apiKey, units, lang).collect { result ->
                _weatherState.value = result
            }
        }
    }
}