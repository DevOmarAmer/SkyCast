package com.example.skycast.data.remote

import com.example.skycast.data.model.WeatherResponse
import retrofit2.Response

interface IRemoteDataSource {
    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Response<WeatherResponse>
}
