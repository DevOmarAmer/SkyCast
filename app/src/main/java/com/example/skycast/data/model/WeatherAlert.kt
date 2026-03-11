package com.example.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alerts")
data class WeatherAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val alertType: String, // alarm or notif
    val isActive: Boolean = true,
    val workerId: String
)