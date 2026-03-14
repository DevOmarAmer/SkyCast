package com.example.skycast.ui.home.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

// ── Daily 5-Day Row ───────────────────────────────────────────────────────────
@Composable
fun DailyForecastRow(forecast: ForecastItem) {
    val dayName = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(forecast.dateText.substring(0, 10))
        SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
    } catch (e: Exception) { forecast.dateText.substring(0, 10) }

    val dateShort = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(forecast.dateText.substring(0, 10))
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(date!!)
    } catch (e: Exception) { "" }

    val iconCode = forecast.weatherInfo.firstOrNull()?.icon

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SkyNavy),
        border = BorderStroke(1.dp, FrostStrong)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.W600, color = CloudWhite)
                Text(text = dateShort, style = MaterialTheme.typography.labelSmall, color = CloudGrey)
            }

            // Icon
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(Modifier.width(12.dp))

            // Temp range
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${forecast.main.tempMax.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W700,
                    color = SunGold
                )
                Text(
                    text = "${forecast.main.tempMin.toInt()}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = CloudGrey
                )
            }
        }
    }
}
