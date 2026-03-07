package com.example.skycast.data.repository


import com.example.skycast.data.local.FavoriteLocationDao
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.WeatherApiService
import com.example.skycast.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

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
    ): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getWeatherForecast(lat, lon, apiKey, units, lang)
            if (response.isSuccessful) {
                response.body()?.let { weatherData ->
                    emit(Resource.Success(weatherData))
                } ?: emit(Resource.Error("حدث خطأ: البيانات فارغة"))
            } else {
                emit(Resource.Error("خطأ في السيرفر: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("خطأ في الاتصال: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("تأكد من اتصالك بالإنترنت."))
        } catch (e: Exception) {
            emit(Resource.Error("حدث خطأ غير متوقع: ${e.localizedMessage}"))
        }

    }



    // --- (Local) Responsibility ---

    fun getFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteDao.getAllFavoriteLocations()

    suspend fun insertFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.insertLocation(location)

    suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        favoriteDao.deleteLocation(location)
}