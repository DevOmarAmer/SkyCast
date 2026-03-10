package com.example.skycast.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    val favoritesList: StateFlow<List<FavoriteLocation>> = repository.getFavoriteLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteLocation(location: FavoriteLocation) {
        viewModelScope.launch {
            repository.deleteFavoriteLocation(location)
        }
    }
}