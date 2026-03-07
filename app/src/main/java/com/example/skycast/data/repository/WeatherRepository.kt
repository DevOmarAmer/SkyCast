package com.example.skycast.data.repository


import com.example.skycast.data.local.FavoriteLocationDao
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val favoriteDao: FavoriteLocationDao
) {

    // (Remote) responsibality

    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ) =
        apiService.getWeatherForecast(
            lat = lat,
            lon = lon,
            apiKey = apiKey,
            units = units,
            lang = lang
        )


    // --- (Local) Responsibility ---

    fun getFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteDao.getAllFavoriteLocations()

    suspend fun insertFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.insertLocation(location)

    suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.deleteLocation(location)
}