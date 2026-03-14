package com.example.skycast.ui.alerts.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.skycast.R
import com.example.skycast.data.remote.RetrofitClient
import com.example.skycast.data.repository.AIAssistantRepositoryImpl
import com.example.skycast.utils.NotificationHelper
import java.util.Calendar

/**
 * Daily periodic worker for the Morning Brief feature.
 * Fetches today's weather and fires a friendly summary notification.
 *
 * Input data keys:
 *  - "lat"     : Double
 *  - "lon"     : Double
 *  - "api_key" : String
 */
class MorningBriefWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lat    = inputData.getDouble(KEY_LAT, 0.0)
        val lon    = inputData.getDouble(KEY_LON, 0.0)
        val apiKey = inputData.getString(KEY_API_KEY) ?: return Result.failure()

        return try {
            val response = RetrofitClient.apiService.getWeatherForecast(lat, lon, apiKey)
            if (!response.isSuccessful) return Result.retry()

            val weather = response.body() ?: return Result.retry()

            // Find first forecast item for today (or current)
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val todayItem = weather.forecastList.firstOrNull { item ->
                val cal = Calendar.getInstance().apply { timeInMillis = item.date * 1000L }
                cal.get(Calendar.DAY_OF_YEAR) == today
            } ?: weather.forecastList.firstOrNull() ?: return Result.success()

            val cityName  = weather.city.name
            val tempC     = todayItem.main.temp.toInt()
            val desc      = todayItem.weatherInfo.firstOrNull()?.description
                ?.replaceFirstChar { it.uppercase() } ?: "Clear"
            val humidity  = todayItem.main.humidity
            val windSpeed = todayItem.wind.speed.toInt()

            val isRainy = (desc.contains("Rain", true) || desc.contains("Drizzle", true) || desc.contains("Thunderstorm", true))

            val aiRepo = AIAssistantRepositoryImpl()
            val lang = context.resources.configuration.locales.get(0).language
            val aiSummary = aiRepo.getWeatherSummary(tempC, desc, windSpeed, isRainy, lang)

            val message = aiSummary?.trim() ?: (context.getString(R.string.ai_error_disclaimer) + buildGreeting(cityName, tempC, desc, humidity, windSpeed))

            NotificationHelper.showMorningBrief(context, message)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun buildGreeting(
        city: String, tempC: Int, desc: String, humidity: Int, wind: Int
    ): String {
        val outfit = when {
            tempC >= 35 -> context.getString(R.string.outfit_hot)
            tempC >= 25 -> context.getString(R.string.outfit_warm)
            tempC >= 15 -> context.getString(R.string.outfit_mild)
            tempC >= 5  -> context.getString(R.string.outfit_cool)
            else        -> context.getString(R.string.outfit_cold)
        }
        return context.getString(R.string.morning_brief_message_format, city, tempC, desc, humidity, wind, outfit)
    }

    companion object {
        const val KEY_LAT     = "lat"
        const val KEY_LON     = "lon"
        const val KEY_API_KEY = "api_key"
        const val UNIQUE_WORK_NAME = "morning_brief_daily"
    }
}
