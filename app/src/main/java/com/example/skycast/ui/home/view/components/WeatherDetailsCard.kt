package com.example.skycast.ui.home.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.R
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.ui.theme.*

@Composable
fun WeatherDetailsCard(currentWeather: ForecastItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Frost),
        border = BorderStroke(1.dp, FrostStrong)
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherStatItem(emoji = "💧", label = stringResource(R.string.humidity), value = "${currentWeather.main.humidity}%")
                VerticalDivider()
                WeatherStatItem(emoji = "💨", label = stringResource(R.string.wind), value = "${currentWeather.wind.speed} m/s")
                VerticalDivider()
                WeatherStatItem(emoji = "🌡️", label = stringResource(R.string.pressure), value = "${currentWeather.main.pressure} hPa")
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = FrostStrong
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherStatItem(emoji = "👁️", label = stringResource(R.string.visibility), value = "${currentWeather.visibility / 1000} km")
                VerticalDivider()
                WeatherStatItem(emoji = "☁️", label = stringResource(R.string.clouds), value = "${currentWeather.clouds.all}%")
                VerticalDivider()
                WeatherStatItem(emoji = "🌫️", label = stringResource(R.string.feels_like_label), value = "${currentWeather.main.feelsLike.toInt()}°")
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(FrostStrong)
    )
}

@Composable
fun WeatherStatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.W700,
            color = CloudWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CloudGrey
        )
    }
}
