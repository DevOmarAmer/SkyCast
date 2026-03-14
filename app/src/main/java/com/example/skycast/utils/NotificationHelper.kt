package com.example.skycast.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.skycast.R
import com.example.skycast.ui.alerts.view.AlarmActivity

object NotificationHelper {

    private const val ALARM_CHANNEL_ID = "com.example.skycast.ALARM"
    private const val NOTIF_CHANNEL_ID = "com.example.skycast.NOTIF"
    private const val BRIEF_CHANNEL_ID = "com.example.skycast.BRIEF"

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
                // Activity handles sound/vibration to loop continuously
                setSound(null, null)
                enableVibration(false)
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

        val notifId = System.currentTimeMillis().toInt()
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)

        if (isAlarm) {
            val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("EXTRA_TITLE", title)
                putExtra("EXTRA_MESSAGE", message)
                putExtra("EXTRA_NOTIF_ID", notifId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            
            // Attempt to forcefully launch the activity directly (works if in foreground, or if app has SYSTEM_ALERT_WINDOW)
            try {
                context.startActivity(fullScreenIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context, notifId, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            // Hide the actual notification content if it shows up as a heads up because we want the activity
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        } else {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
            builder.setVibrate(longArrayOf(0, 300, 150, 300))
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        manager.notify(notifId, builder.build())
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
