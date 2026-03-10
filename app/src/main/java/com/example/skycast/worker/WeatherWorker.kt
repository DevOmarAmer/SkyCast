package com.example.skycast.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.skycast.R

class WeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val alertType = inputData.getString("alert_type") ?: "notification"

        // val weather = repository.getWeatherForecast(...)
        val weatherCondition = "مطر خفيف"

        if (alertType == "notification") {
            showNotification("تنبيه الطقس!", "حالة الطقس الحالية: $weatherCondition")
        } else {
            // will use media player leter to play sound
            showNotification("منبه الطقس ⏰", "استيقظ! حالة الطقس: $weatherCondition")
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "weather_alerts_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // أيقونة مؤقتة
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // يختفي عند الضغط عليه [cite: 45]
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}