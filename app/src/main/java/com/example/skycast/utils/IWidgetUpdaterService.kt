package com.example.skycast.utils

interface IWidgetUpdaterService {
    suspend fun updateWidget(temp: String, city: String, desc: String)
}
