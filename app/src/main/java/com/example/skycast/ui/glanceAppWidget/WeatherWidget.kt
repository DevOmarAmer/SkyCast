package com.example.skycast.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.skycast.MainActivity

class WeatherWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val tempKey = stringPreferencesKey("widget_temp")
        val cityKey = stringPreferencesKey("widget_city")
        val descKey = stringPreferencesKey("widget_desc")

        suspend fun updateWidget(context: Context, temp: String, city: String, desc: String) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[tempKey] = temp
                    prefs[cityKey] = city
                    prefs[descKey] = desc
                }
                WeatherWidget().update(context, glanceId)
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val temp = prefs[tempKey] ?: "--°"
            val city = prefs[cityKey] ?: "Current Location"
            val desc = prefs[descKey] ?: "..."

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0A1628))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = city,
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = temp,
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = desc,
                    style = TextStyle(
                        color = ColorProvider(day = Color.LightGray, night = Color.LightGray),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}