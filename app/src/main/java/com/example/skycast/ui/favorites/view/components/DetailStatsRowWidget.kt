package com.example.skycast.ui.favorites.view.components

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
fun DetailStatsRow(current: ForecastItem) {
    val wc = LocalWeatherColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = wc.cardSurface),
        border = BorderStroke(1.dp, wc.accent.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStatItem(emoji = "💧", label = stringResource(R.string.humidity),  value = "${current.main.humidity}%")
                DetailStatDivider()
                DetailStatItem(emoji = "💨", label = stringResource(R.string.wind),      value = "${current.wind.speed} m/s")
                DetailStatDivider()
                DetailStatItem(emoji = "🌡️", label = stringResource(R.string.pressure),  value = "${current.main.pressure} hPa")
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = wc.accent.copy(alpha = 0.2f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStatItem(emoji = "👁️", label = "Visibility", value = "${current.visibility / 1000} km")
                DetailStatDivider()
                DetailStatItem(emoji = "☁️", label = "Clouds",     value = "${current.clouds.all}%")
                DetailStatDivider()
                DetailStatItem(emoji = "🌫️", label = "Feels Like", value = "${current.main.feelsLike.toInt()}°")
            }
        }
    }
}

@Composable
internal fun DetailStatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.W700, color = CloudWhite)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = CloudGrey)
    }
}

@Composable
internal fun DetailStatDivider() {
    Box(modifier = Modifier.width(1.dp).height(44.dp).background(FrostStrong))
}
