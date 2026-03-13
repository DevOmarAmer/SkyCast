package com.example.skycast.data.repository


import com.example.skycast.data.local.AlertDao
import com.example.skycast.data.local.FavoriteLocationDao
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.WeatherApiService
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

import com.example.skycast.data.local.WeatherDao
import com.example.skycast.data.local.CachedWeather

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val favoriteDao: FavoriteLocationDao,
    private val alertDao: AlertDao,
    private val weatherDao: WeatherDao
): IWeatherRepository {

    // (Remote) responsibality

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getWeatherForecast(lat, lon, apiKey, units, lang)
            if (response.isSuccessful) {
                response.body()?.let { weatherData ->
                    // Cache the successful network response for offline use
                    weatherDao.insertWeather(CachedWeather(1, weatherData))
                    emit(Resource.Success(weatherData))
                } ?: emit(Resource.Error("Data is empty"))
            } else {
                emit(Resource.Error("Server error: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP Error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            // No internet connection, attempt to fetch from local cache
            val localCache = weatherDao.getCachedWeather()
            if (localCache != null) {
                emit(Resource.Success(localCache.weatherResponse))
            } else {
                emit(Resource.Error("Check your internet connection."))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }

    }



    // --- (Local) Responsibility ---

    override fun getFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteDao.getAllFavoriteLocations()

    override suspend fun insertFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.insertLocation(location)

    override suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.deleteLocation(location)

    override fun getAlerts(): Flow<List<WeatherAlert>> = alertDao.getAlerts()

    override suspend fun insertAlert(alert: WeatherAlert) {
        alertDao.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {
        alertDao.deleteAlert(alert)

    }
}