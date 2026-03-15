package com.example.skycast.utils

import java.util.Locale

object UnitUtils {

    fun formatWindSpeed(speed: Double, apiUnits: String, userWindUnit: String): String {
        val baselineIsMetric = apiUnits == "metric" || apiUnits == "standard"
        
        val convertedSpeed = when {
            baselineIsMetric && userWindUnit == "mph" -> speed * 2.23694
            !baselineIsMetric && userWindUnit == "m/s" -> speed * 0.44704
            else -> speed
        }
        
        return String.format(Locale.US, "%.1f %s", convertedSpeed, userWindUnit)
    }
}
