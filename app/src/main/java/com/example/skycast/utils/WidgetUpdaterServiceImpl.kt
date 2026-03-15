package com.example.skycast.utils

import android.content.Context
import com.example.skycast.widget.WidgetUpdater

class WidgetUpdaterServiceImpl(private val context: Context) : IWidgetUpdaterService {
    override suspend fun updateWidget(temp: String, city: String, desc: String) {
        WidgetUpdater.updateWidget(context, temp, city, desc)
    }

    override suspend fun updateAiBrief(brief: String) {
        WidgetUpdater.updateAiBrief(context, brief)
    }
}
