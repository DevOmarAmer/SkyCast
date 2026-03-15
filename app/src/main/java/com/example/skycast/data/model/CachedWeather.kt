package com.example.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeather(
    @PrimaryKey
    val id: Int = 1, // We only store one cached weather response for the home screen
    val weatherResponse: WeatherResponse
)