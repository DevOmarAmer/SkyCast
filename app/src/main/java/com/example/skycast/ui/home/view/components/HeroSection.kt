package com.example.skycast.ui.home.view.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

// ── Hero: City, Date, Big Temp, Icon ─────────────────────────────────────────
@Composable
fun HeroSection(data: WeatherResponse, currentWeather: ForecastItem) {
    // Pulsing glow behind the hero icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val float by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.3f), Color.Transparent))
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // City name
            Text(
                text = data.city.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.W600,
                color = CloudWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Date
            Text(
                text = formatFullDate(currentWeather.dateText),
                style = MaterialTheme.typography.bodyMedium,
                color = CloudGrey
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Glowing icon
            Box(contentAlignment = Alignment.Center) {
                // Ambient glow
                Box(
                    modifier = Modifier
                        .size((120 * glowScale).dp)
                        .scale(glowScale)
                        .background(
                            Brush.radialGradient(
                                listOf(SkyBlueBright.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .blur(20.dp)
                )
                val iconCode = currentWeather.weatherInfo.firstOrNull()?.icon
                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${iconCode}@4x.png",
                    contentDescription = "Weather Icon",
                    modifier = Modifier
                        .size(130.dp)
                        .offset(y = float.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = "${currentWeather.main.temp.toInt()}°",
                fontSize = 80.sp,
                fontWeight = FontWeight.W200,
                color = CloudWhite,
                lineHeight = 80.sp
            )
            Text(
                text = currentWeather.weatherInfo.firstOrNull()?.description?.replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                } ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = SkyBluePale,
                fontWeight = FontWeight.W400
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Min / Max pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TemperaturePill(label = "Feels", value = "${currentWeather.main.feelsLike.toInt()}°", color = SunGold)
                Box(Modifier.size(4.dp).background(FrostStrong, CircleShape))
                TemperaturePill(label = "Min", value = "${currentWeather.main.tempMin.toInt()}°", color = RainBlue)
                Box(Modifier.size(4.dp).background(FrostStrong, CircleShape))
                TemperaturePill(label = "Max", value = "${currentWeather.main.tempMax.toInt()}°", color = StormRed)
            }
        }
    }
}

// ── Temperature pill ──────────────────────────────────────────────────────────
@Composable
private fun TemperaturePill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.W700, fontSize = 15.sp)
        Text(text = label, color = CloudGrey, fontSize = 11.sp)
    }
}

// ── Date Formatter ────────────────────────────────────────────────────────────
fun formatFullDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("EEEE, dd MMM · hh:mm a", Locale.getDefault())
        val date = parser.parse(dateString)
        if (date != null) formatter.format(date) else dateString
    } catch (e: Exception) { dateString }
}
