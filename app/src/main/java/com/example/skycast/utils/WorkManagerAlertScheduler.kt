package com.example.skycast.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.data.service.worker.MorningBriefWorker
import com.example.skycast.data.service.worker.WeatherConditionWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkManagerAlertScheduler(private val context: Context) : IAlertScheduler {

    private val workManager get() = WorkManager.getInstance(context)


    override fun scheduleConditionAlert(
        conditionType: String,
        threshold: Double,
        alertType: String,
        label: String,
        lat: Double,
        lon: Double,
        apiKey: String,
        startDateTime: Long,
        endDateTime: Long
    ): WeatherAlert {
        // Tag is used for cancellation inside the worker once end time is reached
        val uniqueTag = "alert_${System.currentTimeMillis()}"

        val inputData = Data.Builder()
            .putString(WeatherConditionWorker.KEY_CONDITION_TYPE, conditionType)
            .putDouble(WeatherConditionWorker.KEY_THRESHOLD, threshold)
            .putString(WeatherConditionWorker.KEY_ALERT_TYPE, alertType)
            .putString(WeatherConditionWorker.KEY_LABEL, label)
            .putDouble(WeatherConditionWorker.KEY_LAT, lat)
            .putDouble(WeatherConditionWorker.KEY_LON, lon)
            .putString(WeatherConditionWorker.KEY_API_KEY, apiKey)
            .putLong(WeatherConditionWorker.KEY_START_DATETIME, startDateTime)
            .putLong(WeatherConditionWorker.KEY_END_DATETIME, endDateTime)
            .putString(WeatherConditionWorker.KEY_WORK_TAG, uniqueTag)
            .build()

        val request = PeriodicWorkRequestBuilder<WeatherConditionWorker>(
            15, TimeUnit.MINUTES
        )
            .addTag(uniqueTag)
            .setInputData(inputData)
            .build()

        workManager.enqueue(request)

        return WeatherAlert(
            conditionType = conditionType,
            threshold     = threshold,
            label         = label,
            alertType     = alertType,
            workerId      = uniqueTag, // Instead of ID, save tag to cancel by tag
            createdAt     = System.currentTimeMillis(),
            startDateTime = startDateTime,
            endDateTime   = endDateTime
        )
    }

    override fun scheduleMorningBrief(
        hour: Int, minute: Int, lat: Double, lon: Double, apiKey: String
    ): String {
        val delay = calculateDelayUntil(hour, minute)

        val inputData = Data.Builder()
            .putDouble(MorningBriefWorker.KEY_LAT, lat)
            .putDouble(MorningBriefWorker.KEY_LON, lon)
            .putString(MorningBriefWorker.KEY_API_KEY, apiKey)
            .build()

        val request = PeriodicWorkRequestBuilder<MorningBriefWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniquePeriodicWork(
            MorningBriefWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        return MorningBriefWorker.UNIQUE_WORK_NAME
    }

    override fun cancelMorningBrief() {
        workManager.cancelUniqueWork(MorningBriefWorker.UNIQUE_WORK_NAME)
    }

    override fun cancel(workTag: String) {
        try {
            workManager.cancelAllWorkByTag(workTag)
        } catch (_: Exception) {}
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /** Returns the milliseconds until the next occurrence of [hour]:[minute]. */
    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val now    = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
