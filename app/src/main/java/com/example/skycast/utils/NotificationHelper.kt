package com.example.skycast.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.skycast.R

object NotificationHelper {

    private const val ALARM_CHANNEL_ID = "weather_alarm_channel"
    private const val NOTIF_CHANNEL_ID = "weather_notif_channel"
    private const val BRIEF_CHANNEL_ID = "morning_brief_channel"

    // Long rumble: 0ms delay, 500ms on, 200ms off, 800ms on, 200ms off, 500ms on
    private val ALARM_VIBRATION = longArrayOf(0, 500, 200, 800, 200, 500)

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ── Alarm channel – plays alarm ringtone, max importance
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAttr  = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        manager.createNotificationChannel(
            NotificationChannel(ALARM_CHANNEL_ID, context.getString(R.string.channel_weather_alarms), NotificationManager.IMPORTANCE_HIGH).apply {
                description   = context.getString(R.string.channel_weather_alarms_desc)
                setSound(alarmSound, alarmAttr)
                enableVibration(true)
                vibrationPattern = ALARM_VIBRATION
            }
        )

        // ── Notification channel – default notification sound
        val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        manager.createNotificationChannel(
            NotificationChannel(NOTIF_CHANNEL_ID, context.getString(R.string.channel_weather_alerts), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.channel_weather_alerts_desc)
                setSound(notifSound, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableVibration(true)
            }
        )

        // ── Morning brief – gentle, default
        manager.createNotificationChannel(
            NotificationChannel(BRIEF_CHANNEL_ID, context.getString(R.string.channel_morning_brief_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.channel_morning_brief_desc)
            }
        )
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun showWeatherAlert(context: Context, title: String, message: String, isAlarm: Boolean = false) {
        createChannels(context)
        val manager   = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (isAlarm) ALARM_CHANNEL_ID else NOTIF_CHANNEL_ID

        val soundUri = if (isAlarm)
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        else
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(if (isAlarm) ALARM_VIBRATION else longArrayOf(0, 300, 150, 300))
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)

        // Extra: vibrate on older APIs where channel vibration may not apply
        if (isAlarm && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
                ?.vibrate(ALARM_VIBRATION, -1)
        }
    }

    fun showMorningBrief(context: Context, message: String) {
        createChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, BRIEF_CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(context.getString(R.string.morning_brief_notification_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
