package com.example.skycast.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.ui.home.HeroSection
import com.example.skycast.ui.home.HourlyForecastRow
import com.example.skycast.ui.home.SectionHeader
import com.example.skycast.ui.home.WeatherDetailsCard
import com.example.skycast.ui.home.DailyForecastRow
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.Resource
import androidx.compose.foundation.lazy.items

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
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar with back button ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.3f), SkyDeepNavy.copy(alpha = 0f)))
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Frost, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = CloudWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = location.cityName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.W600,
                        color = CloudWhite
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when (weatherState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading forecast for ${location.cityName}…",
                                color = CloudGrey,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val data = (weatherState as Resource.Success).data
                    if (data != null) {
                        val currentWeather = data.forecastList.firstOrNull()
                        val todayDate = data.forecastList.firstOrNull()?.dateText?.substring(0, 10) ?: ""
                        val hourlyToday = data.forecastList.filter { it.dateText.startsWith(todayDate) }
                        val dailyForecasts = data.forecastList
                            .groupBy { it.dateText.substring(0, 10) }
                            .entries.take(5)
                            .map { it.value.first() }

                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (currentWeather != null) {
                                item { HeroSection(data = data, currentWeather = currentWeather) }
                                item { WeatherDetailsCard(currentWeather = currentWeather) }
                            }
                            item { SectionHeader(title = "Today's Forecast") }
                            item { HourlyForecastRow(hourlyItems = hourlyToday) }
                            item { SectionHeader(title = "5-Day Forecast") }
                            items(dailyForecasts) { day ->
                                DailyForecastRow(forecast = day)
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (weatherState as Resource.Error).message ?: "Failed to load weather",
                                color = StormRed,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
