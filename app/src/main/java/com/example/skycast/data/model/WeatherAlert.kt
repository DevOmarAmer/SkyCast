package com.example.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Condition types for smart weather alerts.
 * TEMP_ABOVE / TEMP_BELOW  → threshold in °C.
 * WIND_ABOVE               → threshold in m/s.
 * RAIN_EXPECTED / VERY_CLOUDY ignore threshold.
 */
object AlertCondition {
    const val TEMP_ABOVE     = "TEMP_ABOVE"
    const val TEMP_BELOW     = "TEMP_BELOW"
    const val WIND_ABOVE     = "WIND_ABOVE"
    const val RAIN_EXPECTED  = "RAIN_EXPECTED"
    const val VERY_CLOUDY    = "VERY_CLOUDY"
}

@Entity(tableName = "weather_alerts")
data class WeatherAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conditionType: String,
    val threshold: Double = 0.0,
    val label: String,
    val alertType: String,          // "notification" | "alarm"
    val isActive: Boolean = true,
    val workerId: String,           // UUID or unique tag used to cancel WorkManager job
    val createdAt: Long = System.currentTimeMillis(),
    val startDateTime: Long = System.currentTimeMillis(),
    val endDateTime: Long   = System.currentTimeMillis() + 24L * 60 * 60 * 1000
)