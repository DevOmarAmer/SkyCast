package com.example.skycast.data.remote

import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.ApiService.WeatherApiService
import retrofit2.Response

class RemoteDataSource(
    private val apiService: WeatherApiService
) : IRemoteDataSource {

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Response<WeatherResponse> {
        return apiService.getWeatherForecast(lat, lon, apiKey, units, lang)
    }
}
