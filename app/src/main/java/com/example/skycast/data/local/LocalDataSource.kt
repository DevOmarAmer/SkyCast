package com.example.skycast.data.local

import com.example.skycast.data.local.DAOs.AlertDao
import com.example.skycast.data.local.entity.CachedWeather
import com.example.skycast.data.local.DAOs.FavoriteLocationDao
import com.example.skycast.data.local.DAOs.WeatherDao
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.local.entity.WeatherAlert
import com.example.skycast.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val favoriteDao: FavoriteLocationDao,
    private val alertDao: AlertDao,
    private val weatherDao: WeatherDao
) : ILocalDataSource {

    // --- Cached Weather ---

    override suspend fun insertCachedWeather(weatherResponse: WeatherResponse) {
        weatherDao.insertWeather(CachedWeather(weatherResponse = weatherResponse))
    }

    override suspend fun getCachedWeather(): WeatherResponse? {
        return weatherDao.getCachedWeather()?.weatherResponse
    }

    // --- Favorites ---

    override fun getFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteDao.getAllFavoriteLocations()

    override suspend fun insertFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.insertLocation(location)

    override suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.deleteLocation(location)

    // --- Alerts ---

    override fun getAlerts(): Flow<List<WeatherAlert>> =
        alertDao.getAlerts()

    override suspend fun insertAlert(alert: WeatherAlert) =
        alertDao.insertAlert(alert)

    override suspend fun deleteAlert(alert: WeatherAlert) =
        alertDao.deleteAlert(alert)
}
