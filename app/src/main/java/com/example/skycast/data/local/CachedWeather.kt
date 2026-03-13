package com.example.skycast.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skycast.data.model.WeatherResponse

@Entity(tableName = "cached_weather")
data class CachedWeather(
    @PrimaryKey
    val id: Int = 1, // We only store one cached weather response for the home screen
    val weatherResponse: WeatherResponse
)
