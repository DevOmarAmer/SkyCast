package com.example.skycast.data.repository

interface IAIAssistantRepository {
    suspend fun getWeatherSummary(
        tempC: Int,
        condition: String,
        windSpeed: Int,
        isRainy: Boolean
    ): String
}