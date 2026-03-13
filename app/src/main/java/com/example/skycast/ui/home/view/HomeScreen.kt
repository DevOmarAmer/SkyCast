package com.example.skycast.ui.home.view

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.Resource
import java.text.SimpleDateFormat
import java.util.*


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.skycast.utils.ConnectivityObserver

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyDeepNavy, DarkSurface, SkyNavy)
                )
            )
    ) {
        when (weatherState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching weather…",
                            color = CloudGrey,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is Resource.Success -> {
                val data = (weatherState as Resource.Success).data
                if (data != null) {
                    WeatherContent(data)
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (weatherState as Resource.Error).message ?: "Something went wrong",
                            color = StormRed,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Offline Banner ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = networkStatus == ConnectivityObserver.Status.Unavailable || networkStatus == ConnectivityObserver.Status.Lost,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Offline Mode - Showing Cached Data",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeatherContent(data: WeatherResponse) {
    val currentWeather = data.forecastList.firstOrNull() ?: return

    // Group forecast by day for the 5-day section
    val dailyForecasts = data.forecastList
        .groupBy { it.dateText.substring(0, 10) }
        .entries.take(5)
        .map { it.value.first() } // take first forecast item per day

    // Today's hourly items
    val todayDate = data.forecastList.firstOrNull()?.dateText?.substring(0, 10) ?: ""
    val hourlyToday = data.forecastList.filter { it.dateText.startsWith(todayDate) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = 100.dp
        )
    ) {
        // ── Hero Section ──────────────────────────────────────────────────────
        item {
            HeroSection(data = data, currentWeather = currentWeather)
        }

        // ── Details Card ─────────────────────────────────────────────────────
        item {
            WeatherDetailsCard(currentWeather = currentWeather)
        }

        // ── Today's Hourly Forecast ───────────────────────────────────────────
        item {
            SectionHeader(title = "Today's Forecast")
        }
        item {
            HourlyForecastRow(hourlyItems = hourlyToday)
        }

        // ── 5-Day Forecast ────────────────────────────────────────────────────
        item {
            SectionHeader(title = "5-Day Forecast")
        }
        items(dailyForecasts) { day ->
            DailyForecastRow(forecast = day)
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

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

// ── Glass Details Card ────────────────────────────────────────────────────────
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
                WeatherStatItem(emoji = "👁️", label = "Visibility", value = "${currentWeather.visibility / 1000} km")
                VerticalDivider()
                WeatherStatItem(emoji = "☁️", label = "Clouds", value = "${currentWeather.clouds.all}%")
                VerticalDivider()
                WeatherStatItem(emoji = "🌫️", label = "Feels Like", value = "${currentWeather.main.feelsLike.toInt()}°")
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

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.W600,
        color = CloudWhite,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ── Hourly Forecast Row ───────────────────────────────────────────────────────
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

// ── Hourly Card ───────────────────────────────────────────────────────────────
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

// ── Date Formatter ────────────────────────────────────────────────────────────
fun formatFullDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("EEEE, dd MMM · hh:mm a", Locale.getDefault())
        val date = parser.parse(dateString)
        if (date != null) formatter.format(date) else dateString
    } catch (e: Exception) { dateString }
}