package com.example.skycast.utils

import com.example.skycast.data.local.entity.WeatherAlert

interface IAlertScheduler {
    fun scheduleConditionAlert(
        conditionType: String,
        threshold: Double,
        alertType: String,
        label: String,
        lat: Double,
        lon: Double,
        apiKey: String,
        startDateTime: Long,
        endDateTime: Long
    ): WeatherAlert

    fun scheduleMorningBrief(hour: Int, minute: Int, lat: Double, lon: Double, apiKey: String): String

    fun cancel(workerId: String)
    fun cancelMorningBrief()
}
