package com.example.skycast.utils

import com.example.skycast.data.model.WeatherAlert

interface IAlertScheduler {
    fun schedule(delayMinutes: Long, type: String): WeatherAlert
    fun cancel(workerId: String)
}
