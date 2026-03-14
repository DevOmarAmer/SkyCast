package com.example.skycast.data.repository

interface IAIAssistantRepository {
    suspend fun getWeatherSummary(
        tempC: Int,
        condition: String,
        windSpeed: Int,
        isRainy: Boolean,
        language: String
    ): String?

    suspend fun getDetailedMorningAnalysis(
        cityName: String,
        forecastList: List<com.example.skycast.data.model.ForecastItem>,
        language: String
    ): String?
}