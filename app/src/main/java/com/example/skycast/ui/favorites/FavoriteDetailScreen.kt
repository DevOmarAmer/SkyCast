package com.example.skycast.ui.favorites

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.R
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private const val API_KEY = "59ea0a0dbe5f3beceb5f818d109328ec"

@Composable
fun FavoriteDetailScreen(
    location: FavoriteLocation,
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    val weatherState by viewModel.selectedFavoriteWeather.collectAsState()

    LaunchedEffect(location) {
        viewModel.loadFavoriteWeather(location.latitude, location.longitude, API_KEY)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface, SkyNavy)))
    ) {
        when (weatherState) {
            is Resource.Loading -> DetailLoadingState(location, onNavigateBack)

            is Resource.Success -> {
                val data = (weatherState as Resource.Success).data
                if (data != null) {
                    DetailSuccessContent(data, location, onNavigateBack)
                }
            }

            is Resource.Error -> DetailErrorState(
                message = (weatherState as Resource.Error).message,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

// ── Success: full rich layout ─────────────────────────────────────────────────
@Composable
private fun DetailSuccessContent(
    data: WeatherResponse,
    location: FavoriteLocation,
    onNavigateBack: () -> Unit
) {
    val current = data.forecastList.firstOrNull() ?: return
    val todayDate = current.dateText.substring(0, 10)
    val hourlyToday = data.forecastList.filter { it.dateText.startsWith(todayDate) }
    val dailyForecasts = data.forecastList
        .groupBy { it.dateText.substring(0, 10) }
        .entries.take(5)
        .map { it.value.first() }

    // Pulsing glow behind the hero icon
    val infiniteTransition = rememberInfiniteTransition(label = "detail")
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Hero header with glassmorphism ────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(SkyBlue.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FrostStrong, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = CloudWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center: city + temperature hero
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(FrostStrong, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SkyBluePale,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = location.cityName,
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBluePale,
                            fontWeight = FontWeight.W600
                        )
                    }

                    Spacer(Modifier.height(20.dp))

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
                        val iconCode = current.weatherInfo.firstOrNull()?.icon
                        AsyncImage(
                            model = "https://openweathermap.org/img/wn/${iconCode}@4x.png",
                            contentDescription = "Weather icon",
                            modifier = Modifier
                                .size(130.dp)
                                .offset(y = float.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Temperature
                    Text(
                        text = "${current.main.temp.toInt()}°",
                        fontSize = 80.sp,
                        fontWeight = FontWeight.W200,
                        color = CloudWhite,
                        lineHeight = 80.sp
                    )

                    Text(
                        text = current.weatherInfo.firstOrNull()?.description
                            ?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = SkyBluePale,
                        fontWeight = FontWeight.W400
                    )

                    Spacer(Modifier.height(8.dp))

                    // Min / Max pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TemperaturePill(label = "Feels", value = "${current.main.feelsLike.toInt()}°", color = SunGold)
                        Box(Modifier.size(4.dp).background(FrostStrong, CircleShape))
                        TemperaturePill(label = "Min", value = "${current.main.tempMin.toInt()}°", color = RainBlue)
                        Box(Modifier.size(4.dp).background(FrostStrong, CircleShape))
                        TemperaturePill(label = "Max", value = "${current.main.tempMax.toInt()}°", color = StormRed)
                    }
                }
            }
        }

        // ── Stats row ─────────────────────────────────────────────────────────
        item {
            DetailStatsRow(current)
        }

        // ── Hourly Forecast ───────────────────────────────────────────────────
        item { DetailSectionHeader(stringResource(R.string.today_forecast)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(hourlyToday) { forecast ->
                    DetailHourlyCard(forecast)
                }
            }
        }

        // ── 5-Day Forecast ────────────────────────────────────────────────────
        item { DetailSectionHeader(stringResource(R.string.five_day_forecast)) }
        items(dailyForecasts) { day ->
            DetailDailyCard(day)
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

// ── Stats grid row ────────────────────────────────────────────────────────────
@Composable
private fun DetailStatsRow(current: ForecastItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Frost),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
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
                color = FrostStrong
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
private fun DetailStatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.W700, color = CloudWhite)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = CloudGrey)
    }
}

@Composable
private fun DetailStatDivider() {
    Box(modifier = Modifier.width(1.dp).height(44.dp).background(FrostStrong))
}

// ── Section header ────────────────────────────────────────────────────────────
@Composable
private fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.W600,
        color = CloudWhite,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp, end = 20.dp)
    )
}

// ── Hourly card ───────────────────────────────────────────────────────────────
@Composable
private fun DetailHourlyCard(forecast: ForecastItem) {
    val iconCode = forecast.weatherInfo.firstOrNull()?.icon
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SkyNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
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
                color = RainBlue
            )
        }
    }
}

// ── Daily forecast card ───────────────────────────────────────────────────────
@Composable
private fun DetailDailyCard(forecast: ForecastItem) {
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
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
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

// ── Loading state ────────────────────────────────────────────────────────────
@Composable
private fun DetailLoadingState(location: FavoriteLocation, onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.3f), Color.Transparent)))
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(FrostStrong, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CloudWhite)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(location.cityName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.W600, color = CloudWhite)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.loading_weather), color = CloudGrey, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ── Error state ───────────────────────────────────────────────────────────────
@Composable
private fun DetailErrorState(message: String?, onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("😞", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = message ?: stringResource(R.string.failed_load),
                color = StormRed,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("← Go Back", color = CloudWhite)
            }
        }
    }
}
