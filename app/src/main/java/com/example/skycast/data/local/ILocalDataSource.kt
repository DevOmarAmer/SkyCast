package com.example.skycast.data.local

import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {

    // --- Cached Weather ---
    suspend fun insertCachedWeather(weatherResponse: WeatherResponse)
    suspend fun getCachedWeather(): WeatherResponse?

    // --- Favorites ---
    fun getFavoriteLocations(): Flow<List<FavoriteLocation>>
    suspend fun insertFavoriteLocation(location: FavoriteLocation)
    suspend fun deleteFavoriteLocation(location: FavoriteLocation)

    // --- Alerts ---
    fun getAlerts(): Flow<List<WeatherAlert>>
    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
}
