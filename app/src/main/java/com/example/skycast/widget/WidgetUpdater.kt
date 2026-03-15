package com.example.skycast.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState

object WidgetUpdater {
    suspend fun updateWidget(context: Context, temp: String, city: String, desc: String) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[WeatherWidget.tempKey] = temp
                prefs[WeatherWidget.cityKey] = city
                prefs[WeatherWidget.descKey] = desc
            }
            WeatherWidget().update(context, glanceId)
        }
    }

    suspend fun updateAiBrief(context: Context, brief: String) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(AiBriefWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[AiBriefWidget.briefKey] = brief
            }
            AiBriefWidget().update(context, glanceId)
        }
    }
}
