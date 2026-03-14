package com.example.skycast.ui.favorites.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.skycast.ui.theme.CloudGrey
import com.example.skycast.ui.theme.CloudWhite
import com.example.skycast.ui.theme.LocalWeatherColors

@Composable
fun DetailHourlyCard(forecast: ForecastItem) {
    val wc = LocalWeatherColors.current
    val iconCode = forecast.weatherInfo.firstOrNull()?.icon
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = wc.cardSurface),
        border = BorderStroke(1.dp, wc.accent.copy(alpha = 0.2f))
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
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${forecast.main.temp.toInt()}°",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W700,
                color = CloudWhite
            )
            Text(
                text = "${forecast.main.humidity}%",
                style = MaterialTheme.typography.labelSmall,
                color = wc.accent.copy(alpha = 0.8f)
            )
        }
    }
}
