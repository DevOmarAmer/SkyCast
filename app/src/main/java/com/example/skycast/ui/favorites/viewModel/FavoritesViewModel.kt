package com.example.skycast.ui.favorites.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: IWeatherRepository
) : ViewModel() {

    val favoritesList: StateFlow<List<FavoriteLocation>> = repository.getFavoriteLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFavoriteWeather =
        MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())
    val selectedFavoriteWeather: StateFlow<Resource<WeatherResponse>> =
        _selectedFavoriteWeather.asStateFlow()

    fun loadFavoriteWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            _selectedFavoriteWeather.value = Resource.Loading()
            repository.getWeatherForecast(lat, lon, apiKey, "metric", "en").collect { result ->
                _selectedFavoriteWeather.value = result
            }
        }
    }

    fun deleteLocation(location: FavoriteLocation) {
        viewModelScope.launch { repository.deleteFavoriteLocation(location) }
    }

    fun addLocation(location: FavoriteLocation) {
        viewModelScope.launch { repository.insertFavoriteLocation(location) }
    }
}