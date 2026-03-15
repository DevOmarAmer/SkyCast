package com.example.skycast.utils

interface IWidgetUpdaterService {
    suspend fun updateWidget(temp: String, city: String, desc: String)
    suspend fun updateAiBrief(brief: String)
}
