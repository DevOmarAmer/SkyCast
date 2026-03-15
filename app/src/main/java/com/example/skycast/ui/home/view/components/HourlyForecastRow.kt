package com.example.skycast.ui.home.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun HourlyForecastRow(hourlyItems: List<ForecastItem>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(hourlyItems) { forecast ->
            HourlyCard(forecast = forecast)
        }
    }
}

@Composable
fun HourlyCard(forecast: ForecastItem) {
    val iconCode = forecast.weatherInfo.firstOrNull()?.icon
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SkyNavy),
        border = BorderStroke(1.dp, FrostStrong)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = forecast.dateText.substring(11, 16),
                style = MaterialTheme.typography.labelMedium,
                color = CloudGrey
            )
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${forecast.main.temp.toInt()}°",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W700,
                color = CloudWhite
            )
            Text(
                text = "${forecast.main.humidity}%",
                style = MaterialTheme.typography.labelSmall,
                color = RainBlue
            )
        }
    }
}
