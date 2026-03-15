package com.example.skycast.data.service.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.skycast.R
import com.example.skycast.data.model.AlertCondition
import com.example.skycast.data.remote.ApiService.RetrofitClient
import com.example.skycast.utils.AlertUtils
import com.example.skycast.utils.LocaleHelper
import com.example.skycast.utils.NotificationHelper
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.first

/**
 * Periodic worker (every 15 min) that:
 *  1. Guards: skips if current time is outside [START_DATETIME, END_DATETIME].
 *  2. Self-cancels via unique tag when end time is reached.
 *  3. Fetches current weather and fires a notification when the condition is met.
 *
 * Input data keys  →  see companion object.
 */
class WeatherConditionWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.VIBRATE)
    override suspend fun doWork(): Result {
        val conditionType  = inputData.getString(KEY_CONDITION_TYPE) ?: return Result.failure()
        val threshold      = inputData.getDouble(KEY_THRESHOLD, 0.0)
        val alertType      = inputData.getString(KEY_ALERT_TYPE) ?: "notification"
        val label          = inputData.getString(KEY_LABEL) ?: conditionType
        val lat            = inputData.getDouble(KEY_LAT, 0.0)
        val lon            = inputData.getDouble(KEY_LON, 0.0)
        val apiKey         = inputData.getString(KEY_API_KEY) ?: return Result.failure()
        val startDateTime  = inputData.getLong(KEY_START_DATETIME, 0L)
        val endDateTime    = inputData.getLong(KEY_END_DATETIME, Long.MAX_VALUE)
        val workTag        = inputData.getString(KEY_WORK_TAG) ?: ""

        val now = System.currentTimeMillis()

        // ── Outside the active window → self-cancel if expired ─────────────────
        if (now < startDateTime) return Result.success()   // too early, wait
        if (now > endDateTime) {
            if (workTag.isNotEmpty()) {
                WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
            }
            return Result.success()
        }

        // ── Check weather condition ────────────────────────────────────────────
        return try {
            val settingsManager = SettingsManager(context)
            val savedLang       = settingsManager.langFlow.first()

            val response = RetrofitClient.apiService.getWeatherForecast(lat, lon, apiKey, lang = savedLang)
            if (!response.isSuccessful) return Result.retry()

            val weather = response.body() ?: return Result.retry()
            val current = weather.forecastList.firstOrNull() ?: return Result.success()

            val conditionMet = when (conditionType) {
                AlertCondition.TEMP_ABOVE    -> current.main.temp > threshold
                AlertCondition.TEMP_BELOW    -> current.main.temp < threshold
                AlertCondition.WIND_ABOVE    -> current.wind.speed > threshold
                AlertCondition.RAIN_EXPECTED -> current.weatherInfo.any {
                    it.main.equals("Rain", ignoreCase = true) ||
                    it.main.equals("Drizzle", ignoreCase = true) ||
                    it.main.equals("Thunderstorm", ignoreCase = true)
                }
                AlertCondition.VERY_CLOUDY   -> current.clouds.all >= 80
                else                         -> false
            }

            if (conditionMet) {
                val tempC   = current.main.temp.toInt()
                val desc    = current.weatherInfo.firstOrNull()?.description ?: ""

                val localizedContext = LocaleHelper.getLocalizedContext(context, savedLang)

                val localizedLabel = AlertUtils.buildLocalizedLabel(localizedContext, conditionType, threshold)
                val message = buildMessage(localizedContext, conditionType, threshold, tempC, desc)

                NotificationHelper.showWeatherAlert(
                    context = localizedContext,
                    title   = localizedContext.getString(R.string.alert_title_format, localizedLabel),
                    message = message,
                    isAlarm = alertType == "alarm"
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun buildMessage(localizedContext: Context, type: String, threshold: Double, tempC: Int, desc: String): String = when (type) {
        AlertCondition.TEMP_ABOVE    -> localizedContext.getString(R.string.alert_msg_temp_above, tempC, threshold.toInt(), desc)
        AlertCondition.TEMP_BELOW    -> localizedContext.getString(R.string.alert_msg_temp_below, tempC, threshold.toInt(), desc)
        AlertCondition.WIND_ABOVE    -> localizedContext.getString(R.string.alert_msg_wind_above, threshold.toInt(), desc)
        AlertCondition.RAIN_EXPECTED -> localizedContext.getString(R.string.alert_msg_rain_expected, desc)
        AlertCondition.VERY_CLOUDY   -> localizedContext.getString(R.string.alert_msg_very_cloudy, desc)
        else                         -> localizedContext.getString(R.string.alert_msg_default, desc)
    }

    companion object {
        const val KEY_CONDITION_TYPE  = "condition_type"
        const val KEY_THRESHOLD       = "threshold"
        const val KEY_ALERT_TYPE      = "alert_type"
        const val KEY_LABEL           = "label"
        const val KEY_LAT             = "lat"
        const val KEY_LON             = "lon"
        const val KEY_API_KEY         = "api_key"
        const val KEY_START_DATETIME  = "start_datetime"
        const val KEY_END_DATETIME    = "end_datetime"
        const val KEY_WORK_TAG        = "work_tag"
    }
}