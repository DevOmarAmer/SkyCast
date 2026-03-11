package com.example.skycast.data.repository

import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.Flow

interface IWeatherRepository {
    // remote
    suspend fun getWeatherForecast(lat: Double, lon: Double, apiKey: String, units: String, lang: String): Flow<Resource<WeatherResponse>>

    // local fav
    fun getFavoriteLocations(): Flow<List<FavoriteLocation>>
    suspend fun insertFavoriteLocation(location: FavoriteLocation)
    suspend fun deleteFavoriteLocation(location: FavoriteLocation)

    // loacal alerts
    fun getAlerts(): Flow<List<WeatherAlert>>
    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
}