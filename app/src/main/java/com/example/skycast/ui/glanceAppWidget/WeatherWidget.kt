package com.example.skycast.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider // Correct import added here
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class WeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val temp = "25°C"
        val city = "Alexandria"

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0A1628))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = city,
                    style = TextStyle(
                        // Explicitly set day and night colors
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = temp,
                    style = TextStyle(
                        // Explicitly set day and night colors
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "⛅ Cloudy",
                    style = TextStyle(
                        // Explicitly set day and night colors
                        color = ColorProvider(day = Color.LightGray, night = Color.LightGray),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}