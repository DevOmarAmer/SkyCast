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

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val favoriteDao: FavoriteLocationDao,
    private val alertDao: AlertDao
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
                    emit(Resource.Success(weatherData))
                } ?: emit(Resource.Error("Data is empty"))
            } else {
                emit(Resource.Error("Server error: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP Error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Check your internet connection."))
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