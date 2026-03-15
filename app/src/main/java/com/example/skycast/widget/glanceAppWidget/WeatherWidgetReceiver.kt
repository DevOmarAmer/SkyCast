package com.example.skycast.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    // Link receiver to our widget UI
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()
}