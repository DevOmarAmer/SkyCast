package com.example.skycast.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.skycast.data.model.WeatherAlert
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkManagerAlertScheduler(private val context: Context) : IAlertScheduler {

    override fun schedule(delayMinutes: Long, type: String): WeatherAlert {
        val inputData = Data.Builder()
            .putString("alert_type", type)
            .build()

        val request = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)

        val currentTime = System.currentTimeMillis()
        val triggerTime = currentTime + TimeUnit.MINUTES.toMillis(delayMinutes)

        return WeatherAlert(
            startTime = currentTime,
            endTime = triggerTime,
            alertType = type,
            workerId = request.id.toString()
        )
    }

    override fun cancel(workerId: String) {
        WorkManager.getInstance(context).cancelWorkById(UUID.fromString(workerId))
    }
}
