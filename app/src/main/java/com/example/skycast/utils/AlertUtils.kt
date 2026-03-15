package com.example.skycast.utils

import android.content.Context
import com.example.skycast.R
import com.example.skycast.data.local.entity.AlertCondition

object AlertUtils {
    fun buildLocalizedLabel(context: Context, condType: String, threshold: Double): String {
        return when (condType) {
            AlertCondition.TEMP_ABOVE    -> "${context.getString(R.string.cond_temp_above)}: ${threshold.toInt()}°C"
            AlertCondition.TEMP_BELOW    -> "${context.getString(R.string.cond_temp_below)}: ${threshold.toInt()}°C"
            AlertCondition.WIND_ABOVE    -> "${context.getString(R.string.cond_wind_above)}: ${threshold.toInt()} m/s"
            AlertCondition.RAIN_EXPECTED -> context.getString(R.string.cond_rain_expected)
            AlertCondition.VERY_CLOUDY   -> context.getString(R.string.cond_very_cloudy)
            else                         -> condType
        }
    }
}
