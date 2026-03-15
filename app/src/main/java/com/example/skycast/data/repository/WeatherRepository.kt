package com.example.skycast.data.repository

import com.example.skycast.data.local.ILocalDataSource
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.local.entity.WeatherAlert
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.IRemoteDataSource
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

class WeatherRepository(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource
) : IWeatherRepository {

    // --- Remote ---

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.getWeatherForecast(lat, lon, apiKey, units, lang)
            if (response.isSuccessful) {
                response.body()?.let { weatherData ->
                    // Cache the successful network response for offline use
                    localDataSource.insertCachedWeather(weatherData)
                    emit(Resource.Success(weatherData))
                } ?: emit(Resource.Error("Data is empty"))
            } else {
                emit(Resource.Error("Server error: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP Error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            // No internet connection, attempt to fetch from local cache
            val cachedWeather = localDataSource.getCachedWeather()
            if (cachedWeather != null) {
                emit(Resource.Success(cachedWeather))
            } else {
                emit(Resource.Error("Check your internet connection."))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    // --- Local: Favorites ---

    override fun getFavoriteLocations(): Flow<List<FavoriteLocation>> =
        localDataSource.getFavoriteLocations()

    override suspend fun insertFavoriteLocation(location: FavoriteLocation) =
        localDataSource.insertFavoriteLocation(location)

    override suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        localDataSource.deleteFavoriteLocation(location)

    // --- Local: Alerts ---

    override fun getAlerts(): Flow<List<WeatherAlert>> =
        localDataSource.getAlerts()

    override suspend fun insertAlert(alert: WeatherAlert) =
        localDataSource.insertAlert(alert)

    override suspend fun deleteAlert(alert: WeatherAlert) =
        localDataSource.deleteAlert(alert)
}